package com.example.acadexa;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AddScheduleDialogFragment extends DialogFragment {
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private EditText subjectInput;
    private EditText startTimeInput;
    private EditText endTimeInput;
    private ChipGroup dayChipGroup;
    private Button selectStartTimeBtn;
    private Button selectEndTimeBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private int userId;
    private String initialDay;
    private ScheduleRepository scheduleRepository;
    private Schedule existingSchedule;
    private OnScheduleSavedListener listener;

    public interface OnScheduleSavedListener {
        void onScheduleSaved(Schedule schedule);
    }

    public static AddScheduleDialogFragment newInstance(int userId, String initialDay, OnScheduleSavedListener listener) {
        AddScheduleDialogFragment fragment = new AddScheduleDialogFragment();
        fragment.userId = userId;
        fragment.initialDay = initialDay;
        fragment.listener = listener;
        return fragment;
    }

    public static AddScheduleDialogFragment newEditInstance(int userId, Schedule schedule, OnScheduleSavedListener listener) {
        AddScheduleDialogFragment fragment = new AddScheduleDialogFragment();
        fragment.userId = userId;
        fragment.existingSchedule = schedule;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_schedule, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        subjectInput = view.findViewById(R.id.subjectInput);
        startTimeInput = view.findViewById(R.id.startTimeInput);
        endTimeInput = view.findViewById(R.id.endTimeInput);
        dayChipGroup = view.findViewById(R.id.dayChipGroup);
        selectStartTimeBtn = view.findViewById(R.id.selectStartTimeBtn);
        selectEndTimeBtn = view.findViewById(R.id.selectEndTimeBtn);
        saveBtn = view.findViewById(R.id.saveScheduleBtn);
        cancelBtn = view.findViewById(R.id.cancelScheduleBtn);

        scheduleRepository = new ScheduleRepository(getContext());

        if (existingSchedule != null) {
            subjectInput.setText(existingSchedule.subject);
            startTimeInput.setText(TimeFormatUtils.formatForDisplay(existingSchedule.startTime));
            endTimeInput.setText(TimeFormatUtils.formatForDisplay(existingSchedule.endTime));
            checkDayChip(existingSchedule.day);
            saveBtn.setText("Update Schedule");
        } else if (initialDay != null) {
            checkDayChip(initialDay);
        }

        selectStartTimeBtn.setOnClickListener(v -> showTimePicker(startTimeInput));
        selectEndTimeBtn.setOnClickListener(v -> showTimePicker(endTimeInput));

        saveBtn.setOnClickListener(v -> saveSchedule());
        cancelBtn.setOnClickListener(v -> dismiss());
    }

    private void checkDayChip(String day) {
        for (int i = 0; i < dayChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) dayChipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(day)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void showTimePicker(EditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    String timeStr = TimeFormatUtils.formatForDisplay(hourOfDay, minute);
                    timeInput.setText(timeStr);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void saveSchedule() {
        String subject = subjectInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();

        List<String> selectedDays = new ArrayList<>();
        for (int i = 0; i < dayChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) dayChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedDays.add(chip.getText().toString());
            }
        }

        if (subject.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || selectedDays.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields and select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        AtomicInteger completedTasks = new AtomicInteger(0);
        int totalTasks = selectedDays.size();

        if (existingSchedule != null) {
            String firstDay = selectedDays.get(0);
            Schedule schedule = new Schedule(userId, subject, startTime, endTime, firstDay, existingSchedule.createdAt, now);
            schedule.id = existingSchedule.id;

            scheduleRepository.updateSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                @Override
                public void onSuccess(int result) {
                    Context context = getContext();
                    if (context != null) ReminderAlarmScheduler.scheduleScheduleReminder(context.getApplicationContext(), schedule);
                    checkCompletion(completedTasks, totalTasks, schedule);
                }

                @Override
                public void onError(String error) {
                    checkCompletion(completedTasks, totalTasks, null);
                }
            });

            for (int i = 1; i < selectedDays.size(); i++) {
                Schedule newSched = new Schedule(userId, subject, startTime, endTime, selectedDays.get(i), now, now);
                scheduleRepository.addSchedule(newSched, new ScheduleRepository.ScheduleCallback() {
                    @Override
                    public void onSuccess(int result) {
                        newSched.id = result;
                        Context context = getContext();
                        if (context != null) ReminderAlarmScheduler.scheduleScheduleReminder(context.getApplicationContext(), newSched);
                        checkCompletion(completedTasks, totalTasks, newSched);
                    }

                    @Override
                    public void onError(String error) {
                        checkCompletion(completedTasks, totalTasks, null);
                    }
                });
            }
        } else {
            for (String day : selectedDays) {
                Schedule schedule = new Schedule(userId, subject, startTime, endTime, day, now, now);
                scheduleRepository.addSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                    @Override
                    public void onSuccess(int result) {
                        schedule.id = result;
                        Context context = getContext();
                        if (context != null) ReminderAlarmScheduler.scheduleScheduleReminder(context.getApplicationContext(), schedule);
                        checkCompletion(completedTasks, totalTasks, schedule);
                    }

                    @Override
                    public void onError(String error) {
                        checkCompletion(completedTasks, totalTasks, null);
                    }
                });
            }
        }
    }

    private void checkCompletion(AtomicInteger completedTasks, int totalTasks, Schedule lastSavedSchedule) {
        if (completedTasks.incrementAndGet() == totalTasks) {
            androidx.fragment.app.FragmentActivity activity = getActivity();
            if (activity == null) return;

            activity.runOnUiThread(() -> {
                Context context = getContext();
                if (context == null || !isAdded()) return;

                if (listener != null && lastSavedSchedule != null) {
                    listener.onScheduleSaved(lastSavedSchedule);
                }
                Toast.makeText(context, "Schedule(s) saved successfully!", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
    }
}

package com.example.acadexa;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Calendar;

public class AddScheduleDialogFragment extends DialogFragment {
    private EditText subjectInput;
    private EditText startTimeInput;
    private EditText endTimeInput;
    private Spinner daySpinner;
    private Button selectStartTimeBtn;
    private Button selectEndTimeBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private int userId;
    private ScheduleRepository scheduleRepository;
    private Schedule existingSchedule;
    private OnScheduleSavedListener listener;

    public interface OnScheduleSavedListener {
        void onScheduleSaved(Schedule schedule);
    }

    public static AddScheduleDialogFragment newInstance(int userId, OnScheduleSavedListener listener) {
        AddScheduleDialogFragment fragment = new AddScheduleDialogFragment();
        fragment.userId = userId;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        subjectInput = view.findViewById(R.id.subjectInput);
        startTimeInput = view.findViewById(R.id.startTimeInput);
        endTimeInput = view.findViewById(R.id.endTimeInput);
        daySpinner = view.findViewById(R.id.daySpinner);
        selectStartTimeBtn = view.findViewById(R.id.selectStartTimeBtn);
        selectEndTimeBtn = view.findViewById(R.id.selectEndTimeBtn);
        saveBtn = view.findViewById(R.id.saveScheduleBtn);
        cancelBtn = view.findViewById(R.id.cancelScheduleBtn);

        scheduleRepository = new ScheduleRepository(getContext());

        // Setup day spinner
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        if (existingSchedule != null) {
            subjectInput.setText(existingSchedule.subject);
            startTimeInput.setText(existingSchedule.startTime);
            endTimeInput.setText(existingSchedule.endTime);
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(existingSchedule.day)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
            saveBtn.setText("Update Schedule");
        }

        selectStartTimeBtn.setOnClickListener(v -> showTimePicker(startTimeInput));
        selectEndTimeBtn.setOnClickListener(v -> showTimePicker(endTimeInput));

        saveBtn.setOnClickListener(v -> saveSchedule());
        cancelBtn.setOnClickListener(v -> dismiss());
    }

    private void showTimePicker(EditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                    timeInput.setText(timeStr);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void saveSchedule() {
        String subject = subjectInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();
        String day = daySpinner.getSelectedItem().toString();

        if (subject.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        Schedule schedule = new Schedule(userId, subject, startTime, endTime, day, existingSchedule != null ? existingSchedule.createdAt : now, now);

        if (existingSchedule != null) {
            schedule.id = existingSchedule.id;
            scheduleRepository.updateSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                @Override
                public void onSuccess(int result) {
                    if (listener != null) listener.onScheduleSaved(schedule);
                    Toast.makeText(getContext(), "Schedule updated!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            scheduleRepository.addSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                @Override
                public void onSuccess(int result) {
                    if (listener != null) listener.onScheduleSaved(schedule);
                    Toast.makeText(getContext(), "Schedule added!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

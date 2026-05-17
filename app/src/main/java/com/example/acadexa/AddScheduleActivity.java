package com.example.acadexa;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AddScheduleActivity extends AppCompatActivity {
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private EditText subjectInput;
    private EditText startTimeInput;
    private EditText endTimeInput;
    private ChipGroup dayChipGroup;
    private Button selectStartTimeBtn;
    private Button selectEndTimeBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private ImageView backBtn;
    private int userId;
    private String initialDay;
    private ScheduleRepository scheduleRepository;
    private Schedule existingSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        // Initialize views
        subjectInput = findViewById(R.id.subjectInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        dayChipGroup = findViewById(R.id.dayChipGroup);
        selectStartTimeBtn = findViewById(R.id.selectStartTimeBtn);
        selectEndTimeBtn = findViewById(R.id.selectEndTimeBtn);
        saveBtn = findViewById(R.id.saveScheduleBtn);
        cancelBtn = findViewById(R.id.cancelScheduleBtn);
        backBtn = findViewById(R.id.backBtn);

        scheduleRepository = new ScheduleRepository(this);

        // Get intent data
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        initialDay = intent.getStringExtra("initialDay");
        existingSchedule = (Schedule) intent.getSerializableExtra("schedule");

        // If editing, populate fields
        if (existingSchedule != null) {
            subjectInput.setText(existingSchedule.subject);
            startTimeInput.setText(TimeFormatUtils.formatForDisplay(existingSchedule.startTime));
            endTimeInput.setText(TimeFormatUtils.formatForDisplay(existingSchedule.endTime));
            checkDayChip(existingSchedule.day);
            saveBtn.setText("Update Schedule");
        } else if (initialDay != null) {
            checkDayChip(initialDay);
        }

        // Set click listeners
        selectStartTimeBtn.setOnClickListener(v -> showTimePicker(startTimeInput));
        selectEndTimeBtn.setOnClickListener(v -> showTimePicker(endTimeInput));
        saveBtn.setOnClickListener(v -> saveSchedule());
        cancelBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
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
            Toast.makeText(this, "Please fill in all fields and select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        AtomicInteger completedTasks = new AtomicInteger(0);
        int totalTasks = selectedDays.size();

        if (existingSchedule != null) {
            // Update the existing schedule for the first selected day
            String firstDay = selectedDays.get(0);
            Schedule schedule = new Schedule(userId, subject, startTime, endTime, firstDay, existingSchedule.createdAt, now);
            schedule.id = existingSchedule.id;
            
            scheduleRepository.updateSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                @Override
                public void onSuccess(int result) {
                    ReminderAlarmScheduler.scheduleScheduleReminder(getApplicationContext(), schedule);
                    checkCompletion(completedTasks, totalTasks);
                }

                @Override
                public void onError(String error) {
                    checkCompletion(completedTasks, totalTasks);
                }
            });

            // If more days were selected, add them as new schedules
            for (int i = 1; i < selectedDays.size(); i++) {
                Schedule newSched = new Schedule(userId, subject, startTime, endTime, selectedDays.get(i), now, now);
                scheduleRepository.addSchedule(newSched, new ScheduleRepository.ScheduleCallback() {
                    @Override
                    public void onSuccess(int result) {
                        newSched.id = result;
                        ReminderAlarmScheduler.scheduleScheduleReminder(getApplicationContext(), newSched);
                        checkCompletion(completedTasks, totalTasks);
                    }

                    @Override
                    public void onError(String error) {
                        checkCompletion(completedTasks, totalTasks);
                    }
                });
            }
        } else {
            // Add new schedules for all selected days
            for (String day : selectedDays) {
                Schedule schedule = new Schedule(userId, subject, startTime, endTime, day, now, now);
                scheduleRepository.addSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                    @Override
                    public void onSuccess(int result) {
                        schedule.id = result;
                        ReminderAlarmScheduler.scheduleScheduleReminder(getApplicationContext(), schedule);
                        checkCompletion(completedTasks, totalTasks);
                    }

                    @Override
                    public void onError(String error) {
                        checkCompletion(completedTasks, totalTasks);
                    }
                });
            }
        }
    }

    private void checkCompletion(AtomicInteger completedTasks, int totalTasks) {
        if (completedTasks.incrementAndGet() == totalTasks) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Schedule(s) saved successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }
    }
}

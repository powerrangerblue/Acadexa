package com.example.acadexa;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddScheduleActivity extends AppCompatActivity {
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private EditText subjectInput;
    private EditText startTimeInput;
    private EditText endTimeInput;
    private Spinner daySpinner;
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
        daySpinner = findViewById(R.id.daySpinner);
        selectStartTimeBtn = findViewById(R.id.selectStartTimeBtn);
        selectEndTimeBtn = findViewById(R.id.selectEndTimeBtn);
        saveBtn = findViewById(R.id.saveScheduleBtn);
        cancelBtn = findViewById(R.id.cancelScheduleBtn);
        backBtn = findViewById(R.id.backBtn);

        scheduleRepository = new ScheduleRepository(this);

        // Setup day spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DAYS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        // Get intent data
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        initialDay = intent.getStringExtra("initialDay");
        existingSchedule = (Schedule) intent.getSerializableExtra("schedule");

        // If editing, populate fields
        if (existingSchedule != null) {
            subjectInput.setText(existingSchedule.subject);
            startTimeInput.setText(existingSchedule.startTime);
            endTimeInput.setText(existingSchedule.endTime);
            for (int i = 0; i < DAYS.length; i++) {
                if (DAYS[i].equals(existingSchedule.day)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
            saveBtn.setText("Update Schedule");
        } else if (initialDay != null) {
            for (int i = 0; i < DAYS.length; i++) {
                if (DAYS[i].equals(initialDay)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        }

        // Set click listeners
        selectStartTimeBtn.setOnClickListener(v -> showTimePicker(startTimeInput));
        selectEndTimeBtn.setOnClickListener(v -> showTimePicker(endTimeInput));
        saveBtn.setOnClickListener(v -> saveSchedule());
        cancelBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());
    }

    private void showTimePicker(EditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
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
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        Schedule schedule = new Schedule(userId, subject, startTime, endTime, day, 
                existingSchedule != null ? existingSchedule.createdAt : now, now);

        if (existingSchedule != null) {
            schedule.id = existingSchedule.id;
            scheduleRepository.updateSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                @Override
                public void onSuccess(int result) {
                    Toast.makeText(AddScheduleActivity.this, "Schedule updated!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddScheduleActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            scheduleRepository.addSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                @Override
                public void onSuccess(int result) {
                    Toast.makeText(AddScheduleActivity.this, "Schedule added!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddScheduleActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

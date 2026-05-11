package com.example.acadexa;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dueDateInput;
    private Button selectDateBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private ImageView backBtn;
    private long selectedDueDate = 0;
    private int userId;
    private TaskRepository taskRepository;
    private Task existingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize views
        titleInput = findViewById(R.id.taskTitleInput);
        descriptionInput = findViewById(R.id.taskDescriptionInput);
        dueDateInput = findViewById(R.id.taskDueDateInput);
        selectDateBtn = findViewById(R.id.selectDateBtn);
        saveBtn = findViewById(R.id.saveTaskBtn);
        cancelBtn = findViewById(R.id.cancelTaskBtn);
        backBtn = findViewById(R.id.backBtn);

        taskRepository = new TaskRepository(this);

        // Get intent data
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        existingTask = (Task) intent.getSerializableExtra("task");

        // If editing, populate fields
        if (existingTask != null) {
            titleInput.setText(existingTask.title);
            descriptionInput.setText(existingTask.description);
            selectedDueDate = existingTask.dueDate;
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            dueDateInput.setText(sdf.format(new Date(existingTask.dueDate)));
            saveBtn.setText("Update Task");
        }

        // Set click listeners
        selectDateBtn.setOnClickListener(v -> showDatePicker());
        saveBtn.setOnClickListener(v -> saveTask());
        cancelBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedCalendar.set(Calendar.MINUTE, minute);
                                selectedDueDate = selectedCalendar.getTimeInMillis();
                                
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                                dueDateInput.setText(sdf.format(new Date(selectedDueDate)));
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDueDate == 0) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        Task task = new Task(userId, title, description, selectedDueDate, 
                existingTask != null ? existingTask.status : "pending", 
                existingTask != null ? existingTask.createdAt : now, now);

        if (existingTask != null) {
            task.id = existingTask.id;
            taskRepository.updateTask(task, new TaskRepository.TaskCallback() {
                @Override
                public void onSuccess(int result) {
                    runOnUiThread(() -> {
                        ReminderAlarmScheduler.scheduleTaskReminder(AddTaskActivity.this, task);
                        Toast.makeText(AddTaskActivity.this, "Task updated!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(AddTaskActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            taskRepository.addTask(task, new TaskRepository.TaskCallback() {
                @Override
                public void onSuccess(int result) {
                    task.id = result;
                    runOnUiThread(() -> {
                        ReminderAlarmScheduler.scheduleTaskReminder(AddTaskActivity.this, task);
                        Toast.makeText(AddTaskActivity.this, "Task added!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(AddTaskActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
}

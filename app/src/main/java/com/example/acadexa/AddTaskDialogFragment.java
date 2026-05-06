package com.example.acadexa;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskDialogFragment extends DialogFragment {
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dueDateInput;
    private Button selectDateBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private long selectedDueDate = 0;
    private int userId;
    private TaskRepository taskRepository;
    private Task existingTask;
    private OnTaskSavedListener listener;

    public interface OnTaskSavedListener {
        void onTaskSaved(Task task);
    }

    public static AddTaskDialogFragment newInstance(int userId, OnTaskSavedListener listener) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        fragment.userId = userId;
        fragment.listener = listener;
        return fragment;
    }

    public static AddTaskDialogFragment newEditInstance(int userId, Task task, OnTaskSavedListener listener) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        fragment.userId = userId;
        fragment.existingTask = task;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        titleInput = view.findViewById(R.id.taskTitleInput);
        descriptionInput = view.findViewById(R.id.taskDescriptionInput);
        dueDateInput = view.findViewById(R.id.taskDueDateInput);
        selectDateBtn = view.findViewById(R.id.selectDateBtn);
        saveBtn = view.findViewById(R.id.saveTaskBtn);
        cancelBtn = view.findViewById(R.id.cancelTaskBtn);

        taskRepository = new TaskRepository(getContext());

        if (existingTask != null) {
            titleInput.setText(existingTask.title);
            descriptionInput.setText(existingTask.description);
            selectedDueDate = existingTask.dueDate;
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            dueDateInput.setText(sdf.format(new Date(existingTask.dueDate)));
            saveBtn.setText("Update Task");
        }

        selectDateBtn.setOnClickListener(v -> showDatePicker());

        saveBtn.setOnClickListener(v -> saveTask());
        cancelBtn.setOnClickListener(v -> dismiss());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
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
            Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDueDate == 0) {
            Toast.makeText(getContext(), "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        Task task = new Task(userId, title, description, selectedDueDate, existingTask != null ? existingTask.status : "pending", existingTask != null ? existingTask.createdAt : now, now);

        if (existingTask != null) {
            task.id = existingTask.id;
            taskRepository.updateTask(task, new TaskRepository.TaskCallback() {
                @Override
                public void onSuccess(int result) {
                    if (listener != null) listener.onTaskSaved(task);
                    Toast.makeText(getContext(), "Task updated!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            taskRepository.addTask(task, new TaskRepository.TaskCallback() {
                @Override
                public void onSuccess(int result) {
                    if (listener != null) listener.onTaskSaved(task);
                    Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();
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

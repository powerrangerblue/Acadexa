package com.example.acadexa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {
    private AuthRepository authRepository;
    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;
    private final List<Task> allTasks = new ArrayList<>();
    private int userId;
    private String currentFilter = "all";
    private TextView taskCountText;
    private TextView pendingCountText;
    private TextView completedCountText;
    private TextView emptyStateText;
    private MaterialButton allFilterButton;
    private MaterialButton pendingFilterButton;
    private MaterialButton completedFilterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tasksRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);
        taskRepository = new TaskRepository(this);
        User user = authRepository.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        userId = user.id;

        TextView titleText = findViewById(R.id.tasksTitleText);
        titleText.setText(user.fullName + "'s Tasks");

        taskCountText = findViewById(R.id.taskCountText);
        pendingCountText = findViewById(R.id.pendingCountText);
        completedCountText = findViewById(R.id.completedCountText);
        emptyStateText = findViewById(R.id.tasksEmptyStateText);

        RecyclerView taskRecycler = findViewById(R.id.taskRecyclerView);
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setNestedScrollingEnabled(false);
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        taskRecycler.setAdapter(taskAdapter);

        allFilterButton = findViewById(R.id.allFilterButton);
        pendingFilterButton = findViewById(R.id.pendingFilterButton);
        completedFilterButton = findViewById(R.id.completedFilterButton);

        findViewById(R.id.addTaskButton).setOnClickListener(v -> openTaskForm(null));
        findViewById(R.id.tasksDashboardButton).setOnClickListener(v -> openScreen(DashboardActivity.class));
        findViewById(R.id.tasksCalendarButton).setOnClickListener(v -> openScreen(CalendarActivity.class));
        findViewById(R.id.tasksProfileButton).setOnClickListener(v -> openScreen(ProfileActivity.class));
        findViewById(R.id.tasksNotificationsButton).setOnClickListener(v -> openScreen(NotificationPanelActivity.class));

        allFilterButton.setOnClickListener(v -> setFilter("all"));
        pendingFilterButton.setOnClickListener(v -> setFilter("pending"));
        completedFilterButton.setOnClickListener(v -> setFilter("completed"));

        setFilter("all");
        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
        applyFilter();
    }

    private void updateFilterButtons() {
        allFilterButton.setAlpha("all".equals(currentFilter) ? 1f : 0.65f);
        pendingFilterButton.setAlpha("pending".equals(currentFilter) ? 1f : 0.65f);
        completedFilterButton.setAlpha("completed".equals(currentFilter) ? 1f : 0.65f);
    }

    private void refreshTasks() {
        taskRepository.getTasksByUser(userId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                runOnUiThread(() -> {
                    allTasks.clear();
                    allTasks.addAll(tasks);
                    applyFilter();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(TasksActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });

        taskRepository.getTaskCounts(userId, new TaskRepository.TaskCountCallback() {
            @Override
            public void onSuccess(int total, int pending, int completed) {
                runOnUiThread(() -> {
                    taskCountText.setText(String.valueOf(total));
                    pendingCountText.setText(String.valueOf(pending));
                    completedCountText.setText(String.valueOf(completed));
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(TasksActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void applyFilter() {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if ("pending".equals(currentFilter) && !"pending".equals(task.status)) {
                continue;
            }
            if ("completed".equals(currentFilter) && !"completed".equals(task.status)) {
                continue;
            }
            filteredTasks.add(task);
        }
        taskAdapter.updateTasks(filteredTasks);
        emptyStateText.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openTaskForm(Task task) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("userId", userId);
        if (task != null) {
            intent.putExtra("task", task);
        }
        startActivity(intent);
    }

    private void openScreen(Class<?> destination) {
        startActivity(new Intent(this, destination));
        finish();
    }

    @Override
    public void onEdit(Task task) {
        openTaskForm(task);
    }

    @Override
    public void onDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete task")
                .setMessage("Remove this task from your dashboard?")
                .setPositiveButton("Delete", (dialog, which) -> taskRepository.deleteTask(task, new TaskRepository.TaskCallback() {
                    @Override
                    public void onSuccess(int result) {
                        runOnUiThread(() -> {
                            Toast.makeText(TasksActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                            refreshTasks();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(TasksActivity.this, error, Toast.LENGTH_SHORT).show());
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleComplete(Task task) {
        Task updatedTask = new Task(
                task.userId,
                task.title,
                task.description,
                task.dueDate,
                "completed".equals(task.status) ? "pending" : "completed",
                task.createdAt,
                System.currentTimeMillis());
        updatedTask.id = task.id;
        taskRepository.updateTask(updatedTask, new TaskRepository.TaskCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(() -> {
                    Toast.makeText(TasksActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                    refreshTasks();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(TasksActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}

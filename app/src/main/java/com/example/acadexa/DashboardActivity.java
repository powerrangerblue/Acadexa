package com.example.acadexa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener, ScheduleAdapter.OnScheduleActionListener, NotificationAdapter.OnNotificationActionListener {
    private AuthRepository authRepository;
    private TaskRepository taskRepository;
    private ScheduleRepository scheduleRepository;
    private NotificationRepository notificationRepository;
    private TaskAdapter taskAdapter;
    private ScheduleAdapter scheduleAdapter;
    private NotificationAdapter notificationAdapter;
    private User currentUser;
    private int userId;

    private TextView welcomeText;
    private TextView studentIdText;
    private TextView dateTimeText;
    private TextView totalTasksText;
    private TextView pendingTasksText;
    private TextView completedTasksText;
    private TextView todayClassesText;
    private TextView nextClassText;
    private TextView unreadBadgeText;
    private TextView dashboardTasksEmptyText;
    private TextView dashboardScheduleEmptyText;
    private TextView dashboardNotificationsEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);
        taskRepository = new TaskRepository(this);
        scheduleRepository = new ScheduleRepository(this);
        notificationRepository = new NotificationRepository(this);

        currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        userId = currentUser.id;
        NotificationScheduler.schedule(this);

        bindViews();
        setupLists();
        setupActions();
        refreshDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void bindViews() {
        welcomeText = findViewById(R.id.welcomeText);
        studentIdText = findViewById(R.id.studentIdText);
        dateTimeText = findViewById(R.id.dateTimeText);
        totalTasksText = findViewById(R.id.totalTasksValueText);
        pendingTasksText = findViewById(R.id.pendingTasksValueText);
        completedTasksText = findViewById(R.id.completedTasksValueText);
        todayClassesText = findViewById(R.id.todayClassesValueText);
        nextClassText = findViewById(R.id.nextClassText);
        unreadBadgeText = findViewById(R.id.unreadBadgeText);
        dashboardTasksEmptyText = findViewById(R.id.dashboardTasksEmptyText);
        dashboardScheduleEmptyText = findViewById(R.id.dashboardScheduleEmptyText);
        dashboardNotificationsEmptyText = findViewById(R.id.dashboardNotificationsEmptyText);

        welcomeText.setText("Hello, " + currentUser.fullName);
        studentIdText.setText(currentUser.studentId == null || currentUser.studentId.isEmpty() ? "Student ID not set" : currentUser.studentId);
        updateDateTime();
    }

    private void setupLists() {
        RecyclerView taskRecycler = findViewById(R.id.dashboardTaskRecyclerView);
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setNestedScrollingEnabled(false);
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        taskRecycler.setAdapter(taskAdapter);

        RecyclerView scheduleRecycler = findViewById(R.id.dashboardScheduleRecyclerView);
        scheduleRecycler.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecycler.setNestedScrollingEnabled(false);
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), this);
        scheduleRecycler.setAdapter(scheduleAdapter);

        RecyclerView notificationRecycler = findViewById(R.id.dashboardNotificationRecyclerView);
        notificationRecycler.setLayoutManager(new LinearLayoutManager(this));
        notificationRecycler.setNestedScrollingEnabled(false);
        notificationAdapter = new NotificationAdapter(new ArrayList<>(), this);
        notificationRecycler.setAdapter(notificationAdapter);
    }

    private void setupActions() {
        ImageButton logoutBtn = findViewById(R.id.logoutButton);
        logoutBtn.setOnClickListener(v -> {
            authRepository.logout();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finishAffinity();
        });

        findViewById(R.id.addTaskButton).setOnClickListener(v -> openTaskForm(null));
        findViewById(R.id.addScheduleButton).setOnClickListener(v -> openScheduleForm(null));
        findViewById(R.id.viewNotificationsButton).setOnClickListener(v -> openScreen(NotificationPanelActivity.class));
        findViewById(R.id.refreshDashboardButton).setOnClickListener(v -> refreshDashboard());

        findViewById(R.id.dashboardNavButton).setOnClickListener(v -> refreshDashboard());
        findViewById(R.id.dashboardTasksButton).setOnClickListener(v -> openScreen(TasksActivity.class));
        findViewById(R.id.dashboardCalendarButton).setOnClickListener(v -> openScreen(CalendarActivity.class));
        findViewById(R.id.dashboardProfileButton).setOnClickListener(v -> openScreen(ProfileActivity.class));
    }

    private void refreshDashboard() {
        updateDateTime();
        loadTaskSummary();
        loadTodaySchedules();
        loadTaskPreview();
        loadNotificationsPreview();
    }

    private void updateDateTime() {
        dateTimeText.setText(new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(new Date()));
    }

    private void loadTaskSummary() {
        taskRepository.getTaskCounts(userId, new TaskRepository.TaskCountCallback() {
            @Override
            public void onSuccess(int total, int pending, int completed) {
                runOnUiThread(() -> {
                    totalTasksText.setText(String.valueOf(total));
                    pendingTasksText.setText(String.valueOf(pending));
                    completedTasksText.setText(String.valueOf(completed));
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadTodaySchedules() {
        scheduleRepository.getTodaySchedules(userId, new ScheduleRepository.ScheduleListCallback() {
            @Override
            public void onSuccess(List<Schedule> scheduleList) {
                runOnUiThread(() -> {
                    todayClassesText.setText(String.valueOf(scheduleList.size()));
                    dashboardScheduleEmptyText.setVisibility(scheduleList.isEmpty() ? View.VISIBLE : View.GONE);

                    List<Schedule> preview = new ArrayList<>(scheduleList);
                    scheduleAdapter.updateSchedules(preview.size() > 4 ? preview.subList(0, 4) : preview);

                    Schedule nextClass = null;
                    long soonestStart = Long.MAX_VALUE;
                    long now = System.currentTimeMillis();
                    for (Schedule schedule : scheduleList) {
                        long startMillis = schedule.getStartDateTimeMillis();
                        if (startMillis > now && startMillis < soonestStart) {
                            soonestStart = startMillis;
                            nextClass = schedule;
                        }
                    }

                    if (nextClass != null) {
                        nextClassText.setText(nextClass.subject + " • starts at " + nextClass.startTime);
                    } else if (!scheduleList.isEmpty()) {
                        nextClassText.setText("All classes for today are complete.");
                    } else {
                        nextClassText.setText("No classes scheduled for today.");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadTaskPreview() {
        taskRepository.getPendingTasks(userId, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> taskList) {
                runOnUiThread(() -> {
                    dashboardTasksEmptyText.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
                    List<Task> preview = new ArrayList<>(taskList);
                    taskAdapter.updateTasks(preview.size() > 4 ? preview.subList(0, 4) : preview);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadNotificationsPreview() {
        notificationRepository.getNotificationsByUser(userId, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notificationList) {
                runOnUiThread(() -> {
                    int unreadCount = 0;
                    for (Notification notification : notificationList) {
                        if (!notification.isRead) {
                            unreadCount++;
                        }
                    }
                    unreadBadgeText.setText(String.valueOf(unreadCount));
                    unreadBadgeText.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
                    dashboardNotificationsEmptyText.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);

                    List<Notification> preview = new ArrayList<>(notificationList);
                    notificationAdapter.updateNotifications(preview.size() > 4 ? preview.subList(0, 4) : preview);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openTaskForm(Task task) {
        AddTaskDialogFragment.OnTaskSavedListener listener = savedTask -> refreshDashboard();
        AddTaskDialogFragment fragment = task == null
                ? AddTaskDialogFragment.newInstance(userId, listener)
                : AddTaskDialogFragment.newEditInstance(userId, task, listener);
        fragment.show(getSupportFragmentManager(), "dashboard_task_form");
    }

    private void openScheduleForm(Schedule schedule) {
        AddScheduleDialogFragment.OnScheduleSavedListener listener = savedSchedule -> refreshDashboard();
        AddScheduleDialogFragment fragment = schedule == null
            ? AddScheduleDialogFragment.newInstance(userId, new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date()), listener)
                : AddScheduleDialogFragment.newEditInstance(userId, schedule, listener);
        fragment.show(getSupportFragmentManager(), "dashboard_schedule_form");
    }

    private void openScreen(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    @Override
    public void onEdit(Task task) {
        openTaskForm(task);
    }

    @Override
    public void onDelete(Task task) {
        taskRepository.deleteTask(task, new TaskRepository.TaskCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(DashboardActivity.this::refreshDashboard);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
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
                runOnUiThread(DashboardActivity.this::refreshDashboard);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onEdit(Schedule schedule) {
        openScheduleForm(schedule);
    }

    @Override
    public void onDelete(Schedule schedule) {
        scheduleRepository.deleteSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(DashboardActivity.this::refreshDashboard);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onMarkAsRead(Notification notification) {
        notificationRepository.markAsRead(userId, notification.id, new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(DashboardActivity.this::refreshDashboard);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onDelete(Notification notification) {
        notificationRepository.deleteNotification(userId, notification.id, new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(DashboardActivity.this::refreshDashboard);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}

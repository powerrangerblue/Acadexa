package com.example.acadexa;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;

public class NotificationCheckerService extends Service {
    private static final String TAG = "NotificationChecker";
    private Handler handler = new Handler();
    private Runnable checkNotificationsRunnable;
    private TaskRepository taskRepository;
    private ScheduleRepository scheduleRepository;
    private NotificationRepository notificationRepository;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        NotificationCheckerService getService() {
            return NotificationCheckerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        taskRepository = new TaskRepository(this);
        scheduleRepository = new ScheduleRepository(this);
        notificationRepository = new NotificationRepository(this);
        authRepository = new AuthRepository(this);

        startChecking();
        return START_STICKY;
    }

    private void startChecking() {
        checkNotificationsRunnable = new Runnable() {
            @Override
            public void run() {
                checkForNotifications();
                // Run every 60 seconds
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(checkNotificationsRunnable);
    }

    private void checkForNotifications() {
        User user = authRepository.getCurrentUser();
        if (user == null) return;

        // Check for upcoming tasks
        taskRepository.getPendingTasks(user.id, new TaskRepository.TaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long now = System.currentTimeMillis();
                for (Task task : tasks) {
                    long timeUntilDue = task.dueDate - now;
                    // 30 minutes before due date
                    if (timeUntilDue > 0 && timeUntilDue < 30 * 60 * 1000) {
                        createNotification(user.id, "Task '" + task.title + "' is due soon!", "task");
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching tasks: " + error);
            }
        });

        // Check for upcoming classes
        scheduleRepository.getTodaySchedules(user.id, new ScheduleRepository.ScheduleListCallback() {
            @Override
            public void onSuccess(List<Schedule> schedules) {
                for (Schedule schedule : schedules) {
                    if (schedule.isUpcoming()) {
                        createNotification(user.id, "Your class '" + schedule.subject + "' is coming up at " + schedule.startTime, "schedule");
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching schedules: " + error);
            }
        });
    }

    private void createNotification(int userId, String message, String type) {
        Notification notification = new Notification(userId, message, type, false, System.currentTimeMillis());
        notificationRepository.addNotification(notification, new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(int result) {
                Log.d(TAG, "Notification created: " + message);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error creating notification: " + error);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (checkNotificationsRunnable != null) {
            handler.removeCallbacks(checkNotificationsRunnable);
        }
        Log.d(TAG, "Service destroyed");
    }
}

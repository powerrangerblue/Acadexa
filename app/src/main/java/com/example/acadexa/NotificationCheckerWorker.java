package com.example.acadexa;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class NotificationCheckerWorker extends Worker {
    private static final long ALERT_WINDOW_MILLIS = 30L * 60L * 1000L;
    private static final long DEDUPE_WINDOW_MILLIS = 45L * 60L * 1000L;

    public NotificationCheckerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(AuthRepository.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(AuthRepository.KEY_USER_ID, -1);
        if (userId <= 0) {
            return Result.success();
        }

        try {
            AppDatabase database = AppDatabase.getInstance(context);
            User user = database.userDao().getUserById(userId);
            if (user == null) {
                return Result.retry();
            }

            long now = System.currentTimeMillis();
            checkTasks(database, userId, now);
            checkSchedules(database, userId, now);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    private void checkTasks(AppDatabase database, int userId, long now) {
        List<Task> tasks = database.taskDao().getPendingTasks(userId);
        NotificationDao notificationDao = database.notificationDao();
        for (Task task : tasks) {
            if (!task.isDueWithinMinutes(30)) {
                continue;
            }

            String message = "Task \"" + task.title + "\" is due within 30 minutes.";
            if (notificationDao.getRecentNotificationCount(userId, "task", message, now - DEDUPE_WINDOW_MILLIS) > 0) {
                continue;
            }

            Notification notification = new Notification(userId, message, "task", false, now);
            long insertedId = notificationDao.insertNotification(notification);
            AppNotificationUtils.show(getApplicationContext(), "Task reminder", message, (int) insertedId);
        }
    }

    private void checkSchedules(AppDatabase database, int userId, long now) {
        List<Schedule> schedules = database.scheduleDao().getSchedulesByUser(userId);
        NotificationDao notificationDao = database.notificationDao();
        for (Schedule schedule : schedules) {
            if (!schedule.isScheduledSoon() && !schedule.isStartingWithinMinutes(30)) {
                continue;
            }

            String message = "Class \"" + schedule.subject + "\" starts at " + schedule.startTime + ".";
            if (notificationDao.getRecentNotificationCount(userId, "schedule", message, now - DEDUPE_WINDOW_MILLIS) > 0) {
                continue;
            }

            Notification notification = new Notification(userId, message, "schedule", false, now);
            long insertedId = notificationDao.insertNotification(notification);
            AppNotificationUtils.show(getApplicationContext(), "Class reminder", message, (int) insertedId);
        }
    }
}

package com.example.acadexa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public final class ReminderAlarmScheduler {
    private static final long TASK_REMINDER_WINDOW_MILLIS = 30L * 60L * 1000L;

    private ReminderAlarmScheduler() {
    }

    public static void scheduleTaskReminder(Context context, Task task) {
        if (task == null) {
            return;
        }

        long triggerAtMillis = task.dueDate - TASK_REMINDER_WINDOW_MILLIS;
        if (triggerAtMillis < System.currentTimeMillis()) {
            if (task.dueDate > System.currentTimeMillis()) {
                triggerAtMillis = System.currentTimeMillis() + 5000L;
            } else {
                return;
            }
        }

        scheduleExactAlarm(context, task.userId, task.id, "task", "Task reminder", "Task \"" + task.title + "\" is due within 30 minutes.", triggerAtMillis);
    }

    public static void scheduleScheduleReminder(Context context, Schedule schedule) {
        if (schedule == null) {
            return;
        }

        long triggerAtMillis = calculateScheduleReminderTime(schedule);
        if (triggerAtMillis <= 0) {
            return;
        }

        scheduleExactAlarm(context, schedule.userId, schedule.id, "schedule", "Class reminder", "Class \"" + schedule.subject + "\" starts at " + schedule.startTime + ".", triggerAtMillis);
    }

    public static void cancelReminder(Context context, String type, int itemId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_TYPE, type);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, itemId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(type, itemId),
                intent,
                getPendingIntentFlags(PendingIntent.FLAG_NO_CREATE));

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static void scheduleExactAlarm(Context context, int userId, int itemId, String type, String title, String message, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_USER_ID, userId);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, itemId);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_TYPE, type);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_TITLE, title);
        intent.putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(type, itemId),
                intent,
                getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT));

        Intent showIntent = new Intent(context, DashboardActivity.class);
        PendingIntent showPendingIntent = PendingIntent.getActivity(
            context,
            getRequestCode(type + "_show", itemId),
            showIntent,
            getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT));

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    private static long calculateScheduleReminderTime(Schedule schedule) {
        try {
            Calendar now = Calendar.getInstance();
            int targetDayOfWeek = dayNameToCalendarDayOfWeek(schedule.day);
            if (targetDayOfWeek == -1) {
                return -1;
            }

            int[] timeParts = TimeFormatUtils.parseToHourMinute(schedule.startTime);
            if (timeParts == null) {
                return -1;
            }

            Calendar classStart = Calendar.getInstance();
            classStart.set(Calendar.HOUR_OF_DAY, timeParts[0]);
            classStart.set(Calendar.MINUTE, timeParts[1]);
            classStart.set(Calendar.SECOND, 0);
            classStart.set(Calendar.MILLISECOND, 0);

            while (classStart.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                classStart.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (classStart.getTimeInMillis() < now.getTimeInMillis()) {
                classStart.add(Calendar.DAY_OF_MONTH, 7);
            }

            long reminderTime = classStart.getTimeInMillis() - TASK_REMINDER_WINDOW_MILLIS;
            if (reminderTime < now.getTimeInMillis()) {
                if (classStart.getTimeInMillis() > now.getTimeInMillis()) {
                    return now.getTimeInMillis() + 5000L;
                }
                return -1;
            }
            return reminderTime;
        } catch (Exception e) {
            return -1;
        }
    }

    private static int dayNameToCalendarDayOfWeek(String day) {
        if (day == null) {
            return -1;
        }

        switch (day) {
            case "Sunday":
                return Calendar.SUNDAY;
            case "Monday":
                return Calendar.MONDAY;
            case "Tuesday":
                return Calendar.TUESDAY;
            case "Wednesday":
                return Calendar.WEDNESDAY;
            case "Thursday":
                return Calendar.THURSDAY;
            case "Friday":
                return Calendar.FRIDAY;
            case "Saturday":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }

    private static int getRequestCode(String type, int itemId) {
        int typeHash = type == null ? 0 : type.hashCode();
        return 31 * typeHash + itemId;
    }

    private static int getPendingIntentFlags(int baseFlag) {
        int flags = baseFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }
}

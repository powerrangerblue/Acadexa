package com.example.acadexa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderAlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_TYPE = "extra_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                String title = intent.getStringExtra(EXTRA_TITLE);
                String message = intent.getStringExtra(EXTRA_MESSAGE);
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());
                int userId = intent.getIntExtra(EXTRA_USER_ID, -1);
                String type = intent.getStringExtra(EXTRA_TYPE);

                if (title == null || message == null) {
                    return;
                }

                AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
                long now = System.currentTimeMillis();
                Notification notification = new Notification(userId, message, type == null ? "reminder" : type, false, now);
                long insertedId = database.notificationDao().insertNotification(notification);
                AppNotificationUtils.show(context, title, message, (int) insertedId);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}

package com.example.acadexa;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class AppNotificationUtils {
    public static final String CHANNEL_ID = "acadexa_alerts";
    private static final String CHANNEL_NAME = "Acadexa Alerts";
    private static final String CHANNEL_DESCRIPTION = "Academic reminders and dashboard alerts";

    private static final String[] MOTIVATIONS = {
        "You've got this! \uD83D\uDCAA",
        "Make today amazing! \u2728",
        "Stay focused and conquer the day!",
        "Believe in yourself, you're doing great!",
        "One step at a time, you're unstoppable! \uD83D\uDE80",
        "Shine bright and have a wonderful day!",
        "Keep up the great work! \uD83C\uDF1F"
    };

    private AppNotificationUtils() {
    }

    public static String formatCatchyMessage(String baseMessage) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 0 && hour < 12) {
            greeting = "Good Morning \u2600\uFE0F";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon \uD83C\uDF1E";
        } else {
            greeting = "Good Evening \uD83C\uDF11";
        }

        String motivation = MOTIVATIONS[(int) (Math.random() * MOTIVATIONS.length)];

        return greeting + "! " + baseMessage + " " + motivation;
    }

    public static boolean shouldShowNotification(Context context, String type, int itemId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("NotificationDedupe", Context.MODE_PRIVATE);
        String key = type + "_" + itemId;
        long lastShown = prefs.getLong(key, 0);
        long now = System.currentTimeMillis();
        
        // 45 minutes deduplication window
        if (now - lastShown < 45L * 60L * 1000L) {
            return false;
        }
        
        prefs.edit().putLong(key, now).apply();
        return true;
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }

            NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(CHANNEL_DESCRIPTION);
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void show(Context context, String title, String message, int notificationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ensureChannel(context);

        Intent launchIntent = new Intent(context, DashboardActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}

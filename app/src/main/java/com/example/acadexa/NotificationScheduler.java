package com.example.acadexa;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class NotificationScheduler {
    private static final String UNIQUE_WORK_NAME = "acadexa_notification_checks";

    private NotificationScheduler() {
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(NotificationCheckerWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .addTag("notification_check")
                .build();
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request);

            androidx.work.OneTimeWorkRequest immediateCheck = new androidx.work.OneTimeWorkRequest.Builder(NotificationCheckerWorker.class)
                .setInitialDelay(0, TimeUnit.SECONDS)
                .addTag("notification_check_immediate")
                .build();
            WorkManager.getInstance(context).enqueue(immediateCheck);
            }
}

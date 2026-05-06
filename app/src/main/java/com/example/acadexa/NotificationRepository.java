package com.example.acadexa;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationRepository {
    private NotificationDao notificationDao;
    private ExecutorService executorService;

    public NotificationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.notificationDao = db.notificationDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void addNotification(Notification notification, NotificationCallback callback) {
        executorService.execute(() -> {
            try {
                long result = notificationDao.insertNotification(notification);
                if (callback != null) callback.onSuccess((int) result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getNotificationsByUser(int userId, NotificationListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Notification> notifications = notificationDao.getNotificationsByUser(userId);
                if (callback != null) callback.onSuccess(notifications);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getUnreadNotifications(int userId, NotificationListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Notification> notifications = notificationDao.getUnreadNotifications(userId);
                if (callback != null) callback.onSuccess(notifications);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getUnreadCount(int userId, NotificationCountCallback callback) {
        executorService.execute(() -> {
            try {
                int count = notificationDao.getUnreadNotificationCount(userId);
                if (callback != null) callback.onSuccess(count);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getRecentNotificationCount(int userId, String type, String message, long since, NotificationCountCallback callback) {
        executorService.execute(() -> {
            try {
                int count = notificationDao.getRecentNotificationCount(userId, type, message, since);
                if (callback != null) callback.onSuccess(count);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void markAsRead(int userId, int notificationId, NotificationCallback callback) {
        executorService.execute(() -> {
            try {
                notificationDao.markAsRead(userId, notificationId);
                if (callback != null) callback.onSuccess(1);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void markAllAsRead(int userId, NotificationCallback callback) {
        executorService.execute(() -> {
            try {
                notificationDao.markAllAsRead(userId);
                if (callback != null) callback.onSuccess(1);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteNotification(int userId, int notificationId, NotificationCallback callback) {
        executorService.execute(() -> {
            try {
                notificationDao.deleteNotificationById(userId, notificationId);
                if (callback != null) callback.onSuccess(1);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    // Callbacks
    public interface NotificationCallback {
        void onSuccess(int result);
        void onError(String error);
    }

    public interface NotificationListCallback {
        void onSuccess(List<Notification> notifications);
        void onError(String error);
    }

    public interface NotificationCountCallback {
        void onSuccess(int count);
        void onError(String error);
    }
}

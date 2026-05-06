package com.example.acadexa;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    long insertNotification(Notification notification);

    @Update
    int updateNotification(Notification notification);

    @Delete
    int deleteNotification(Notification notification);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    List<Notification> getNotificationsByUser(int userId);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    List<Notification> getUnreadNotifications(int userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    int getUnreadNotificationCount(int userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND type = :type AND message = :message AND createdAt >= :since")
    int getRecentNotificationCount(int userId, String type, String message, long since);

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId AND id = :notificationId")
    void markAsRead(int userId, int notificationId);

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    void markAllAsRead(int userId);

    @Query("DELETE FROM notifications WHERE userId = :userId AND id = :notificationId")
    void deleteNotificationById(int userId, int notificationId);
}

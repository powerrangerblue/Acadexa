package com.example.acadexa;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("userId")})
public class Notification {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    public String message;

    public String type; // "task", "schedule", "system", etc.

    public boolean isRead;

    public long createdAt; // epoch millis

    public Notification(int userId, String message, String type, boolean isRead, long createdAt) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
}

package com.example.acadexa;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("userId")})
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    public String title;

    public String description;

    public long dueDate; // epoch millis

    public String status; // "pending", "completed"

    public long createdAt; // epoch millis

    public long updatedAt; // epoch millis

    public Task(int userId, String title, String description, long dueDate, String status, long createdAt, long updatedAt) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isUrgent() {
        long now = System.currentTimeMillis();
        long diff = dueDate - now;
        return diff > 0 && diff < 24 * 60 * 60 * 1000; // within 24 hours
    }

    public boolean isOverdue() {
        return System.currentTimeMillis() > dueDate && "pending".equals(status);
    }

    public boolean isDueWithinMinutes(long minutes) {
        long now = System.currentTimeMillis();
        long diff = dueDate - now;
        return diff > 0 && diff <= minutes * 60 * 1000;
    }
}

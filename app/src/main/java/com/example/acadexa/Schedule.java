package com.example.acadexa;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

@Entity(tableName = "schedules",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("userId")})
public class Schedule {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    public String subject;

    public String startTime; // format: "HH:mm"

    public String endTime; // format: "HH:mm"

    public String day; // "Monday", "Tuesday", etc. or date format

    public long createdAt; // epoch millis

    public long updatedAt; // epoch millis

    public Schedule(int userId, String subject, String startTime, String endTime, String day, long createdAt, long updatedAt) {
        this.userId = userId;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        String today = sdf.format(new Date());
        return today.equals(day);
    }

    public boolean isUpcoming() {
        long startMillis = getStartDateTimeMillis();
        return startMillis > 0 && System.currentTimeMillis() < startMillis;
    }

    public long getStartDateTimeMillis() {
        if (!isToday()) {
            return -1;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date parsed = sdf.parse(startTime);
            if (parsed == null) {
                return -1;
            }

            Calendar now = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            start.setTime(parsed);
            start.set(Calendar.YEAR, now.get(Calendar.YEAR));
            start.set(Calendar.MONTH, now.get(Calendar.MONTH));
            start.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            return start.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean hasStarted() {
        long startMillis = getStartDateTimeMillis();
        return startMillis > 0 && System.currentTimeMillis() >= startMillis;
    }

    public boolean isStartingWithinMinutes(long minutes) {
        long startMillis = getStartDateTimeMillis();
        if (startMillis <= 0) {
            return false;
        }

        long diff = startMillis - System.currentTimeMillis();
        return diff > 0 && diff <= minutes * 60 * 1000;
    }
}

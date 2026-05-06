package com.example.acadexa;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRepository {
    private ScheduleDao scheduleDao;
    private ExecutorService executorService;

    public ScheduleRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.scheduleDao = db.scheduleDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void addSchedule(Schedule schedule, ScheduleCallback callback) {
        executorService.execute(() -> {
            try {
                long result = scheduleDao.insertSchedule(schedule);
                if (callback != null) callback.onSuccess((int) result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void updateSchedule(Schedule schedule, ScheduleCallback callback) {
        executorService.execute(() -> {
            try {
                int result = scheduleDao.updateSchedule(schedule);
                if (callback != null) callback.onSuccess(result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteSchedule(Schedule schedule, ScheduleCallback callback) {
        executorService.execute(() -> {
            try {
                int result = scheduleDao.deleteSchedule(schedule);
                if (callback != null) callback.onSuccess(result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getSchedulesByUser(int userId, ScheduleListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Schedule> schedules = scheduleDao.getSchedulesByUser(userId);
                if (callback != null) callback.onSuccess(schedules);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getScheduleByDay(int userId, String day, ScheduleListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Schedule> schedules = scheduleDao.getScheduleByDay(userId, day);
                if (callback != null) callback.onSuccess(schedules);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getTodaySchedules(int userId, ScheduleListCallback callback) {
        executorService.execute(() -> {
            try {
                // Get today's day name
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault());
                String today = sdf.format(new java.util.Date());
                List<Schedule> schedules = scheduleDao.getScheduleByDay(userId, today);
                if (callback != null) callback.onSuccess(schedules);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    // Callbacks
    public interface ScheduleCallback {
        void onSuccess(int result);
        void onError(String error);
    }

    public interface ScheduleListCallback {
        void onSuccess(List<Schedule> schedules);
        void onError(String error);
    }
}

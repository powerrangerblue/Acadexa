package com.example.acadexa;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private TaskDao taskDao;
    private ExecutorService executorService;

    public TaskRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.taskDao = db.taskDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void addTask(Task task, TaskCallback callback) {
        executorService.execute(() -> {
            try {
                long result = taskDao.insertTask(task);
                if (callback != null) callback.onSuccess((int) result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void updateTask(Task task, TaskCallback callback) {
        executorService.execute(() -> {
            try {
                int result = taskDao.updateTask(task);
                if (callback != null) callback.onSuccess(result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteTask(Task task, TaskCallback callback) {
        executorService.execute(() -> {
            try {
                int result = taskDao.deleteTask(task);
                if (callback != null) callback.onSuccess(result);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getTasksByUser(int userId, TaskListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Task> tasks = taskDao.getTasksByUser(userId);
                if (callback != null) callback.onSuccess(tasks);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getPendingTasks(int userId, TaskListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Task> tasks = taskDao.getPendingTasks(userId);
                if (callback != null) callback.onSuccess(tasks);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getCompletedTasks(int userId, TaskListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Task> tasks = taskDao.getCompletedTasks(userId);
                if (callback != null) callback.onSuccess(tasks);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getTaskCounts(int userId, TaskCountCallback callback) {
        executorService.execute(() -> {
            try {
                int total = taskDao.getTotalTaskCount(userId);
                int pending = taskDao.getPendingTaskCount(userId);
                int completed = taskDao.getCompletedTaskCount(userId);
                if (callback != null) callback.onSuccess(total, pending, completed);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    // Callbacks
    public interface TaskCallback {
        void onSuccess(int result);
        void onError(String error);
    }

    public interface TaskListCallback {
        void onSuccess(List<Task> tasks);
        void onError(String error);
    }

    public interface TaskCountCallback {
        void onSuccess(int total, int pending, int completed);
        void onError(String error);
    }
}

package com.example.acadexa;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insertTask(Task task);

    @Update
    int updateTask(Task task);

    @Delete
    int deleteTask(Task task);

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC")
    List<Task> getTasksByUser(int userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = 'pending' ORDER BY dueDate ASC")
    List<Task> getPendingTasks(int userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = 'completed' ORDER BY updatedAt DESC")
    List<Task> getCompletedTasks(int userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId AND id = :taskId")
    Task getTaskById(int userId, int taskId);

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND status = 'pending'")
    int getPendingTaskCount(int userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND status = 'completed'")
    int getCompletedTaskCount(int userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId")
    int getTotalTaskCount(int userId);
}

package com.example.acadexa;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {

    @Insert
    long insertSchedule(Schedule schedule);

    @Update
    int updateSchedule(Schedule schedule);

    @Delete
    int deleteSchedule(Schedule schedule);

    @Query("SELECT * FROM schedules WHERE userId = :userId ORDER BY startTime ASC")
    List<Schedule> getSchedulesByUser(int userId);

    @Query("SELECT * FROM schedules WHERE userId = :userId AND day = :day ORDER BY startTime ASC")
    List<Schedule> getScheduleByDay(int userId, String day);

    @Query("SELECT * FROM schedules WHERE userId = :userId AND id = :scheduleId")
    Schedule getScheduleById(int userId, int scheduleId);

    @Query("SELECT COUNT(*) FROM schedules WHERE userId = :userId AND day = :day")
    int getScheduleCountByDay(int userId, String day);
}

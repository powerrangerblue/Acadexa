package com.example.acadexa;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE studentId = :studentId LIMIT 1")
    User getUserByStudentId(String studentId);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);
}

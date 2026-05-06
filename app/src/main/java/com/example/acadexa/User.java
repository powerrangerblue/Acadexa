package com.example.acadexa;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {
        @Index(value = {"email"}, unique = true),
        @Index(value = {"studentId"}, unique = true)
})
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fullName;

    public String email;

    public String studentId; // optional but unique if present

    public String password; // hashed password

    public long createdAt; // epoch millis

    public User(String fullName, String email, String studentId, String password, long createdAt) {
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
        this.password = password;
        this.createdAt = createdAt;
    }
}

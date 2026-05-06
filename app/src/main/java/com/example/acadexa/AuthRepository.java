package com.example.acadexa;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.UUID;

public class AuthRepository {
    private final AppDatabase db;
    private final UserDao userDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final String PREFS_NAME = "acadexa_prefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_SESSION_TOKEN = "session_token";

    public AuthRepository(Context context) {
        db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class Result {
        public final boolean success;
        public final String message;
        public final User user;

        public Result(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    public Result register(final String fullName, final String email, final String studentId, final String password) {
        if (fullName == null || fullName.trim().isEmpty()) return new Result(false, "Full name required", null);
        if ((email == null || email.trim().isEmpty()) && (studentId == null || studentId.trim().isEmpty()))
            return new Result(false, "Provide email or student ID", null);

        if (email != null && !email.trim().isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return new Result(false, "Invalid email format", null);

        if (password == null || password.length() < 8) return new Result(false, "Password must be at least 8 characters", null);

        try {
            Future<Result> future = executor.submit(new Callable<Result>() {
                @Override
                public Result call() {
                    // Check duplicates
                    if (email != null && !email.trim().isEmpty()) {
                        User existing = userDao.getUserByEmail(email.trim().toLowerCase());
                        if (existing != null) return new Result(false, "Email already in use", null);
                    }
                    if (studentId != null && !studentId.trim().isEmpty()) {
                        User existing = userDao.getUserByStudentId(studentId.trim());
                        if (existing != null) return new Result(false, "Student ID already in use", null);
                    }

                    String normalizedEmail = (email != null) ? email.trim().toLowerCase() : null;
                    String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                    User user = new User(fullName.trim(), normalizedEmail, (studentId != null ? studentId.trim() : null), hashed, System.currentTimeMillis());
                    long id = userDao.insertUser(user);
                    if (id > 0) {
                        user.id = (int) id;
                        // DO NOT auto-login on registration; user must log in manually
                        return new Result(true, "Registered successfully. Please log in.", user);
                    }
                    return new Result(false, "Failed to register", null);
                }
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return new Result(false, "Registration error: " + e.getMessage(), null);
        }
    }

    public Result login(final String emailOrStudentId, final String password) {
        if (emailOrStudentId == null || emailOrStudentId.trim().isEmpty()) return new Result(false, "Enter email or student ID", null);
        if (password == null || password.isEmpty()) return new Result(false, "Enter password", null);

        try {
            Future<Result> future = executor.submit(new Callable<Result>() {
                @Override
                public Result call() {
                    String input = emailOrStudentId.trim();
                    User user = null;
                    if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                        user = userDao.getUserByEmail(input.toLowerCase());
                    } else {
                        user = userDao.getUserByStudentId(input);
                    }
                    if (user == null) return new Result(false, "Invalid credentials", null);
                    boolean ok = BCrypt.checkpw(password, user.password);
                    if (!ok) return new Result(false, "Invalid credentials", null);
                    prefs.edit().putInt(KEY_USER_ID, user.id).apply();
                    prefs.edit().putString(KEY_SESSION_TOKEN, UUID.randomUUID().toString()).apply();
                    return new Result(true, "Logged in", user);
                }
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return new Result(false, "Login error: " + e.getMessage(), null);
        }
    }

    public void logout() {
        prefs.edit().remove(KEY_USER_ID).remove(KEY_SESSION_TOKEN).apply();
    }

    public User getCurrentUser() {
        int uid = prefs.getInt(KEY_USER_ID, -1);
        if (uid <= 0) return null;
        try {
            Future<User> f = executor.submit(new Callable<User>() {
                @Override
                public User call() {
                    return userDao.getUserById(uid);
                }
            });
            return f.get();
        } catch (Exception e) {
            return null;
        }
    }

    public String getSessionToken() {
        return prefs.getString(KEY_SESSION_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null && getSessionToken() != null;
    }
}

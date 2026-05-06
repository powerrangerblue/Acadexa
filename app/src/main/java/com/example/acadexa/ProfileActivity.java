package com.example.acadexa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private AuthRepository authRepository;
    private TaskRepository taskRepository;
    private ScheduleRepository scheduleRepository;
    private TextView taskSummaryText;
    private TextView scheduleSummaryText;
    private TextView sessionTokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);
        taskRepository = new TaskRepository(this);
        scheduleRepository = new ScheduleRepository(this);

        User user = authRepository.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        TextView nameText = findViewById(R.id.profileNameText);
        TextView idText = findViewById(R.id.profileStudentIdText);
        TextView emailText = findViewById(R.id.profileEmailText);
        TextView joinedText = findViewById(R.id.profileJoinedText);
        sessionTokenText = findViewById(R.id.profileSessionTokenText);
        taskSummaryText = findViewById(R.id.profileTaskSummaryText);
        scheduleSummaryText = findViewById(R.id.profileScheduleSummaryText);

        nameText.setText(user.fullName);
        idText.setText(user.studentId == null || user.studentId.isEmpty() ? "No student ID saved" : user.studentId);
        emailText.setText(user.email == null || user.email.isEmpty() ? "No email saved" : user.email);
        joinedText.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(user.createdAt)));
        String sessionToken = authRepository.getSessionToken();
        if (sessionToken == null || sessionToken.isEmpty()) {
            sessionTokenText.setText("Session not available");
        } else {
            int visibleLength = Math.min(8, sessionToken.length());
            sessionTokenText.setText(sessionToken.substring(0, visibleLength) + "...");
        }

        loadSummary(user.id);

        findViewById(R.id.profileDashboardButton).setOnClickListener(v -> openScreen(DashboardActivity.class));
        findViewById(R.id.profileTasksButton).setOnClickListener(v -> openScreen(TasksActivity.class));
        findViewById(R.id.profileCalendarButton).setOnClickListener(v -> openScreen(CalendarActivity.class));
        findViewById(R.id.profileNotificationsButton).setOnClickListener(v -> openScreen(NotificationPanelActivity.class));
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            authRepository.logout();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finishAffinity();
        });
    }

    private void loadSummary(int userId) {
        taskRepository.getTaskCounts(userId, new TaskRepository.TaskCountCallback() {
            @Override
            public void onSuccess(int total, int pending, int completed) {
                runOnUiThread(() -> taskSummaryText.setText(total + " total • " + pending + " pending • " + completed + " completed"));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> taskSummaryText.setText(error));
            }
        });

        scheduleRepository.getTodaySchedules(userId, new ScheduleRepository.ScheduleListCallback() {
            @Override
            public void onSuccess(java.util.List<Schedule> schedules) {
                runOnUiThread(() -> scheduleSummaryText.setText(schedules.size() + " classes today"));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> scheduleSummaryText.setText(error));
            }
        });
    }

    private void openScreen(Class<?> destination) {
        startActivity(new Intent(this, destination));
        finish();
    }
}

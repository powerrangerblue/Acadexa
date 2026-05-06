package com.example.acadexa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationPanelActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationActionListener {
    private AuthRepository authRepository;
    private NotificationRepository notificationRepository;
    private NotificationAdapter notificationAdapter;
    private final List<Notification> notifications = new ArrayList<>();
    private int userId;
    private TextView unreadCountText;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificationsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);
        notificationRepository = new NotificationRepository(this);
        User user = authRepository.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        userId = user.id;

        TextView titleText = findViewById(R.id.notificationsTitleText);
        titleText.setText(user.fullName + "'s Notifications");
        unreadCountText = findViewById(R.id.unreadCountText);
        emptyStateText = findViewById(R.id.notificationsEmptyStateText);

        RecyclerView recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        notificationAdapter = new NotificationAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(notificationAdapter);

        MaterialButton markAllReadButton = findViewById(R.id.markAllReadButton);
        markAllReadButton.setOnClickListener(v -> markAllAsRead());

        findViewById(R.id.notificationsDashboardButton).setOnClickListener(v -> openScreen(DashboardActivity.class));
        findViewById(R.id.notificationsTasksButton).setOnClickListener(v -> openScreen(TasksActivity.class));
        findViewById(R.id.notificationsCalendarButton).setOnClickListener(v -> openScreen(CalendarActivity.class));
        findViewById(R.id.notificationsProfileButton).setOnClickListener(v -> openScreen(ProfileActivity.class));

        refreshNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotifications();
    }

    private void refreshNotifications() {
        notificationRepository.getNotificationsByUser(userId, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notificationList) {
                runOnUiThread(() -> {
                    notifications.clear();
                    notifications.addAll(notificationList);
                    notificationAdapter.updateNotifications(notificationList);
                    emptyStateText.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(NotificationPanelActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });

        notificationRepository.getUnreadCount(userId, new NotificationRepository.NotificationCountCallback() {
            @Override
            public void onSuccess(int count) {
                runOnUiThread(() -> unreadCountText.setText(String.valueOf(count)));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(NotificationPanelActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void markAllAsRead() {
        notificationRepository.markAllAsRead(userId, new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(() -> {
                    Toast.makeText(NotificationPanelActivity.this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                    refreshNotifications();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(NotificationPanelActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openScreen(Class<?> destination) {
        startActivity(new Intent(this, destination));
        finish();
    }

    @Override
    public void onMarkAsRead(Notification notification) {
        notificationRepository.markAsRead(userId, notification.id, new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(int result) {
                runOnUiThread(() -> refreshNotifications());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(NotificationPanelActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onDelete(Notification notification) {
        new AlertDialog.Builder(this)
                .setTitle("Delete notification")
                .setMessage("Remove this notification?")
                .setPositiveButton("Delete", (dialog, which) -> notificationRepository.deleteNotification(userId, notification.id, new NotificationRepository.NotificationCallback() {
                    @Override
                    public void onSuccess(int result) {
                        runOnUiThread(() -> refreshNotifications());
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(NotificationPanelActivity.this, error, Toast.LENGTH_SHORT).show());
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

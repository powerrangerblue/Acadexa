package com.example.acadexa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepo = new AuthRepository(this);
        User user = authRepo.getCurrentUser();

        TextView welcome = findViewById(R.id.welcomeText);
        TextView dateTime = findViewById(R.id.dateTimeText);

        if (user != null && user.fullName != null && !user.fullName.isEmpty()) {
            welcome.setText("Hello, " + user.fullName);
        } else {
            welcome.setText("Hello") ;
        }

        String now = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(new Date());
        dateTime.setText(now);

        ImageButton logoutBtn = findViewById(R.id.logoutButton);
        logoutBtn.setOnClickListener(v -> {
            authRepo.logout();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        });

        // quick actions could be wired later
        findViewById(R.id.addTaskButton).setOnClickListener(v -> {
            // placeholder: open Tasks or create task
        });

        findViewById(R.id.viewScheduleButton).setOnClickListener(v -> {
            // placeholder: open Calendar
        });
    }
}

package com.example.acadexa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity implements ScheduleAdapter.OnScheduleActionListener {
    private AuthRepository authRepository;
    private ScheduleRepository scheduleRepository;
    private ScheduleAdapter scheduleAdapter;
    private final List<Schedule> schedules = new ArrayList<>();
    private int userId;
    private long selectedDateMillis;
    private TextView selectedDayText;
    private TextView selectedDateLabel;
    private TextView scheduleCountText;
    private MaterialButton addScheduleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendarRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);
        scheduleRepository = new ScheduleRepository(this);
        User user = authRepository.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        userId = user.id;

        TextView titleText = findViewById(R.id.calendarTitleText);
        titleText.setText(user.fullName + "'s Calendar");

        CalendarView calendarView = findViewById(R.id.monthCalendarView);
        selectedDayText = findViewById(R.id.selectedDayText);
        selectedDateLabel = findViewById(R.id.selectedDateLabel);
        scheduleCountText = findViewById(R.id.scheduleCountText);
        addScheduleButton = findViewById(R.id.addScheduleButton);

        RecyclerView scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setNestedScrollingEnabled(false);
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), this);
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        addScheduleButton.setOnClickListener(v -> openScheduleForm(null));
        findViewById(R.id.calendarDashboardButton).setOnClickListener(v -> openScreen(DashboardActivity.class));
        findViewById(R.id.calendarTasksButton).setOnClickListener(v -> openScreen(TasksActivity.class));
        findViewById(R.id.calendarProfileButton).setOnClickListener(v -> openScreen(ProfileActivity.class));
        findViewById(R.id.calendarNotificationsButton).setOnClickListener(v -> openScreen(NotificationPanelActivity.class));

        selectedDateMillis = System.currentTimeMillis();
        updateSelectedDateLabels(selectedDateMillis);
        loadSchedulesForSelectedDay();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = calendar.getTimeInMillis();
            updateSelectedDateLabels(selectedDateMillis);
            loadSchedulesForSelectedDay();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedulesForSelectedDay();
    }

    private void updateSelectedDateLabels(long dateMillis) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        selectedDayText.setText(dayFormat.format(new Date(dateMillis)));
        selectedDateLabel.setText(dateFormat.format(new Date(dateMillis)));
    }

    private void loadSchedulesForSelectedDay() {
        String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date(selectedDateMillis));
        scheduleRepository.getScheduleByDay(userId, dayName, new ScheduleRepository.ScheduleListCallback() {
            @Override
            public void onSuccess(List<Schedule> scheduleList) {
                runOnUiThread(() -> {
                    schedules.clear();
                    schedules.addAll(scheduleList);
                    scheduleAdapter.updateSchedules(scheduleList);
                    scheduleCountText.setText(String.valueOf(scheduleList.size()));
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CalendarActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openScheduleForm(Schedule schedule) {
        AddScheduleDialogFragment.OnScheduleSavedListener listener = savedSchedule -> loadSchedulesForSelectedDay();
        AddScheduleDialogFragment fragment = schedule == null
                ? AddScheduleDialogFragment.newInstance(userId, listener)
                : AddScheduleDialogFragment.newEditInstance(userId, schedule, listener);
        fragment.show(getSupportFragmentManager(), "schedule_form");
    }

    private void openScreen(Class<?> destination) {
        startActivity(new Intent(this, destination));
        finish();
    }

    @Override
    public void onEdit(Schedule schedule) {
        openScheduleForm(schedule);
    }

    @Override
    public void onDelete(Schedule schedule) {
        new AlertDialog.Builder(this)
                .setTitle("Delete schedule")
                .setMessage("Remove this class from the calendar?")
                .setPositiveButton("Delete", (dialog, which) -> scheduleRepository.deleteSchedule(schedule, new ScheduleRepository.ScheduleCallback() {
                    @Override
                    public void onSuccess(int result) {
                        runOnUiThread(() -> {
                            Toast.makeText(CalendarActivity.this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                            loadSchedulesForSelectedDay();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(CalendarActivity.this, error, Toast.LENGTH_SHORT).show());
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

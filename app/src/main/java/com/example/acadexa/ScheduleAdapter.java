package com.example.acadexa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private List<Schedule> schedules;
    private OnScheduleActionListener listener;

    public interface OnScheduleActionListener {
        void onEdit(Schedule schedule);
        void onDelete(Schedule schedule);
    }

    public ScheduleAdapter(List<Schedule> schedules, OnScheduleActionListener listener) {
        this.schedules = schedules;
        this.listener = listener;
    }

    public void updateSchedules(List<Schedule> newSchedules) {
        this.schedules = newSchedules;
        notifyDataSetChanged();
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        Schedule schedule = schedules.get(position);
        holder.bind(schedule);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private TextView subjectView;
        private TextView timeView;
        private TextView dayView;
        private ImageButton editBtn;
        private ImageButton deleteBtn;

        ScheduleViewHolder(View itemView) {
            super(itemView);
            subjectView = itemView.findViewById(R.id.scheduleSubject);
            timeView = itemView.findViewById(R.id.scheduleTime);
            dayView = itemView.findViewById(R.id.scheduleDay);
            editBtn = itemView.findViewById(R.id.editScheduleBtn);
            deleteBtn = itemView.findViewById(R.id.deleteScheduleBtn);
        }

        void bind(Schedule schedule) {
            subjectView.setText(schedule.subject);
            timeView.setText(TimeFormatUtils.formatForDisplay(schedule.startTime) + " - " + TimeFormatUtils.formatForDisplay(schedule.endTime));
            dayView.setText(schedule.day);

            // Highlight current/upcoming class
            if (schedule.isUpcoming()) {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.light_blue));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.white));
            }

            editBtn.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(schedule);
            });

            deleteBtn.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(schedule);
            });
        }
    }
}

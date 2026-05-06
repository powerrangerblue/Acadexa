package com.example.acadexa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
        void onToggleComplete(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView descriptionView;
        private TextView dueDateView;
        private TextView statusView;
        private Button completeBtn;
        private ImageButton editBtn;
        private ImageButton deleteBtn;

        TaskViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.taskTitle);
            descriptionView = itemView.findViewById(R.id.taskDescription);
            dueDateView = itemView.findViewById(R.id.taskDueDate);
            statusView = itemView.findViewById(R.id.taskStatus);
            completeBtn = itemView.findViewById(R.id.completeTaskBtn);
            editBtn = itemView.findViewById(R.id.editTaskBtn);
            deleteBtn = itemView.findViewById(R.id.deleteTaskBtn);
        }

        void bind(Task task) {
            titleView.setText(task.title);
            descriptionView.setText(task.description);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            dueDateView.setText("Due: " + sdf.format(new Date(task.dueDate)));
            
            statusView.setText(task.status.substring(0, 1).toUpperCase() + task.status.substring(1));
            
            if ("completed".equals(task.status)) {
                statusView.setTextColor(itemView.getContext().getColor(R.color.green));
                completeBtn.setText("Undo");
            } else {
                statusView.setTextColor(itemView.getContext().getColor(R.color.orange));
                completeBtn.setText("Complete");
            }

            if (task.isUrgent() && "pending".equals(task.status)) {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.light_red));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.white));
            }

            completeBtn.setOnClickListener(v -> {
                if (listener != null) listener.onToggleComplete(task);
            });

            editBtn.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(task);
            });

            deleteBtn.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(task);
            });
        }
    }
}

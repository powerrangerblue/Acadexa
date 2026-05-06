package com.example.acadexa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onMarkAsRead(Notification notification);
        void onDelete(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView messageView;
        private TextView typeView;
        private TextView timeView;
        private ImageButton markReadBtn;
        private ImageButton deleteBtn;

        NotificationViewHolder(View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.notificationMessage);
            typeView = itemView.findViewById(R.id.notificationType);
            timeView = itemView.findViewById(R.id.notificationTime);
            markReadBtn = itemView.findViewById(R.id.markReadBtn);
            deleteBtn = itemView.findViewById(R.id.deleteNotificationBtn);
        }

        void bind(Notification notification) {
            messageView.setText(notification.message);
            typeView.setText(notification.type);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            timeView.setText(sdf.format(new Date(notification.createdAt)));

            // Highlight unread notifications
            if (!notification.isRead) {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.light_yellow));
                markReadBtn.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.white));
                markReadBtn.setVisibility(View.GONE);
            }

            markReadBtn.setOnClickListener(v -> {
                if (listener != null) listener.onMarkAsRead(notification);
            });

            deleteBtn.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(notification);
            });
        }
    }
}

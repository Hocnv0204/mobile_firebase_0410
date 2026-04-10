package com.hocnv.mobile_0410.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hocnv.mobile_0410.R;

public final class NotificationHelper {
    private NotificationHelper() {}

    public static final String CHANNEL_ID = "showtime_reminders";

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel existing = nm.getNotificationChannel(CHANNEL_ID);
        if (existing != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Nhắc giờ chiếu",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Thông báo nhắc trước giờ chiếu phim");
        nm.createNotificationChannel(channel);
    }

    public static void showReminder(Context context, int notificationId, String title, String body) {
        ensureChannel(context);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, b.build());
    }
}


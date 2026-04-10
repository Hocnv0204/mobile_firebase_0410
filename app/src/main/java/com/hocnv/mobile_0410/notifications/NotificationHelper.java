package com.hocnv.mobile_0410.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hocnv.mobile_0410.MainActivity;
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

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("tab", "tickets");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, b.build());
    }
}


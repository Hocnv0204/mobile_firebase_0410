package com.hocnv.mobile_0410.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hocnv.mobile_0410.util.TimeUtils;

public class ShowtimeReminderWorker extends Worker {
    public static final String KEY_MOVIE_TITLE = "movie_title";
    public static final String KEY_SHOWTIME_MILLIS = "showtime_millis";
    public static final String KEY_THEATER_NAME = "theater_name";

    public ShowtimeReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String title = getInputData().getString(KEY_MOVIE_TITLE);
        long showtimeMillis = getInputData().getLong(KEY_SHOWTIME_MILLIS, -1);
        String theaterName = getInputData().getString(KEY_THEATER_NAME);

        String safeTitle = title == null ? "Phim" : title;
        String safeTheater = theaterName == null ? "" : theaterName;

        String notiTitle = "Nhắc giờ chiếu";
        String notiBody;
        if (showtimeMillis > 0 && !safeTheater.isEmpty()) {
            String time = TimeUtils.formatDateTime(showtimeMillis);
            notiBody = "Phim " + safeTitle + " sắp bắt đầu lúc " + time + " tại " + safeTheater + "!";
        } else if (showtimeMillis > 0) {
            notiBody = "Phim " + safeTitle + " sắp bắt đầu lúc " + TimeUtils.formatDateTime(showtimeMillis) + "!";
        } else {
            notiBody = "Phim " + safeTitle + " sắp bắt đầu!";
        }

        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        NotificationHelper.showReminder(getApplicationContext(), notificationId, notiTitle, notiBody);
        return Result.success();
    }
}


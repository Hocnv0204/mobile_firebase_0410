package com.hocnv.mobile_0410.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hocnv.mobile_0410.util.TimeUtils;

public class ShowtimeReminderWorker extends Worker {
    public static final String KEY_MOVIE_TITLE = "movie_title";
    public static final String KEY_SHOWTIME_MILLIS = "showtime_millis";

    public ShowtimeReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String title = getInputData().getString(KEY_MOVIE_TITLE);
        long showtimeMillis = getInputData().getLong(KEY_SHOWTIME_MILLIS, -1);

        String safeTitle = title == null ? "Phim sắp chiếu" : title;
        String body = showtimeMillis > 0
                ? ("Giờ chiếu: " + TimeUtils.formatDateTime(showtimeMillis))
                : "Sắp đến giờ chiếu của bạn.";

        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        NotificationHelper.showReminder(getApplicationContext(), notificationId, "Nhắc giờ chiếu: " + safeTitle, body);
        return Result.success();
    }
}


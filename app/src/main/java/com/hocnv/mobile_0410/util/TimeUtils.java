package com.hocnv.mobile_0410.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeUtils {
    private TimeUtils() {}

    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static String formatDateTime(long millis) {
        return DATE_TIME.format(new Date(millis));
    }
}


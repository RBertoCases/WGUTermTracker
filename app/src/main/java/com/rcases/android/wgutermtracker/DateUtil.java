package com.rcases.android.wgutermtracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    public static long getDateTimestamp(String dateInput) {
        try {
            Date date = DateUtil.dateFormat.parse(dateInput + TimeZone.getDefault().getDisplayName());
            return date.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public static long todayLong() {
        String currentDate = DateUtil.dateFormat.format(new Date());
        return getDateTimestamp(currentDate);
    }

    public static long todayLongWithTime() {
        return System.currentTimeMillis();
    }
}

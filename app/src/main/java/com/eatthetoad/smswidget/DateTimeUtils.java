package com.eatthetoad.smswidget;

import static android.text.format.DateUtils.isToday;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public static final DateTimeFormatter dtfYearMonthDayTime = DateTimeFormatter.ofPattern("yyyy MMM d h:mm a");
    public static final DateTimeFormatter dtfMonthDayTime = DateTimeFormatter.ofPattern("M/d h:mm a");
    public static final DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("h:mm a");

    public static String getFormattedDate(long date, boolean showShortDate) {
        if (isToday(date) && showShortDate) {
            return dtfTime.format(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()));
        }
        if (isToday(date + android.text.format.DateUtils.DAY_IN_MILLIS) && showShortDate) {
            return "Yesterday " +  dtfTime.format(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()));
        }
        if (showShortDate) {
            return dtfMonthDayTime.format(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()));
        }
        return dtfYearMonthDayTime.format(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()));
    }
}

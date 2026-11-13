package com.example.taskmanager.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateHelper {
    private static final String[] DAY_NAMES = {
            "CHỦ NHẬT", "THỨ HAI", "THỨ BA", "THỨ TƯ", "THỨ NĂM", "THỨ SÁU", "THỨ BẢY"
    };
    private static final double DAY_MS = 24 * 60 * 60 * 1000.0;

    private DateHelper() {}

    /** 00:00 của hôm nay. */
    public static long today() {
        return startOfDay(System.currentTimeMillis());
    }

    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** 13/11/2026 */
    public static String formatFull(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(millis));
    }

    /** 13/11 */
    public static String formatShort(long millis) {
        return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(new Date(millis));
    }

    /** Số ngày từ hôm nay đến ngày đó (âm = đã quá hạn). */
    public static int daysFromToday(long millis) {
        return (int) Math.round((startOfDay(millis) - today()) / DAY_MS);
    }

    /** "13/11 · Hôm nay", "14/11 · Ngày mai", "10/11 · Quá hạn", hoặc "20/11". */
    public static String formatItemDate(long millis) {
        int d = daysFromToday(millis);
        String s = formatShort(millis);
        if (d == 0) return s + " · Hôm nay";
        if (d == 1) return s + " · Ngày mai";
        if (d < 0) return s + " · Quá hạn";
        return s;
    }

    /** "THỨ SÁU, 13 THÁNG 11" */
    public static String headerLabel() {
        Calendar c = Calendar.getInstance();
        return DAY_NAMES[c.get(Calendar.DAY_OF_WEEK) - 1]
                + ", " + c.get(Calendar.DAY_OF_MONTH)
                + " THÁNG " + (c.get(Calendar.MONTH) + 1);
    }
}

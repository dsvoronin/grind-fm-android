package com.dsvoronin.grindfm.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtil {

    private static SimpleDateFormat PARSER_GRIND = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);

    private static SimpleDateFormat FORMATTER = new SimpleDateFormat("d MMMM yyyy");

    public static String formatDate(Date date) {
        return FORMATTER.format(date);
    }

    public static Date parseDate(String rawDate) {
        try {
            return PARSER_GRIND.parse(rawDate);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static Date parseYoutubeDate(String rawDate) {
        final String TAG = "StringUtil.parseYoutubeDate";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        String minusTimezone = rawDate.substring(0, rawDate.indexOf('.'));
        String cleanSpecials = minusTimezone.replace('T', ' ');

        try {
            return sdf.parse(cleanSpecials);
        } catch (ParseException pe) {
            Log.e(TAG, "Error while parsing date: " + cleanSpecials + " with pattern: " + pattern, pe);
            return null;
        }
    }
}

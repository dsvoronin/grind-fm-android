package com.dsvoronin.grindfm.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getYoutubeId(String url) {
        String pattern = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getThumbnail(String youtubeId) {
        return "http://i.ytimg.com/vi/" + youtubeId + "/hqdefault.jpg";
    }

    public static String widgetString(String s) {
        String[] splitted = s.split("-", 2);
        splitted[0] = "artist: " + splitted[0];
        splitted[1] = "song:" + splitted[1];
        return splitted[0] + "\n" + splitted[1];
    }
}

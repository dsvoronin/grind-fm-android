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
    private static SimpleDateFormat PARSER_YOUTUBE = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);

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

    public static String getImage(String description) {
        String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern p = Pattern.compile(imgRegex);
        Matcher m = p.matcher(description);
        try {
            m.find();
            return m.group(1);
        } catch (Exception e) {
            Log.e("StringUtil", "Error while parsing image src", e);
            return null;
        }
    }

    public static String clearHTML(String dirtyString) {
        final String HTML_REGEXP = "<.*?>";
        return dirtyString.replaceAll(HTML_REGEXP, "").trim();
    }

    public static String clearSpecialChars(String str) {
        if (str.contains("&amp;")) {
            str = str.replace("&amp;", "&");
        } else if (str.contains("&lt;")) {
            str = str.replace("&lt;", "<");
        } else if (str.contains("&gt;")) {
            str = str.replace("&gt;", ">");
        } else if (str.contains("&apos;")) {
            str = str.replace("&apos;", "'");
        } else if (str.contains("&quot;")) {
            str = str.replace("&quot;", "\"");
        } else if (str.contains("&laquo;")) {
            str = str.replace("&laquo;", "\"");
        } else if (str.contains("&raquo;")) {
            str = str.replace("&raquo;", "\"");
        }
        return str;
    }
}

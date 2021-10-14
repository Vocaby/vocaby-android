package com.vocaby.app.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Locale;

public class StringFormatter {
    public static String cleanText(String text) {
        return text.trim().replaceAll("[^\\p{L}0-9'-._ ]", "").toLowerCase();
    }

    public static String firstLetterUpperOnly(String text) {
        return text.length() < 1 ?
                text.toUpperCase() : text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static String cleanNumber(int num) {
        return NumberFormat.getNumberInstance(Locale.US).format(num);
    }
}

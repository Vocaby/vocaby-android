package com.vocaby.app.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StringFormatter {
    public static String cleanText(String text) {
        return text.trim().replaceAll("[^\\p{L}0-9'-._ ]", "").toLowerCase();
    }

    public static String firstLetterUpperOnly(String text) {
        return text.length() < 1 ?
                text.toUpperCase() : text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
    }
}

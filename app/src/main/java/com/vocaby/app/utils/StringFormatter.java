package com.vocaby.app.utils;

public class StringFormatter {
    public static String cleanText(String text) {
        return text.trim().toLowerCase().replaceAll("[^0-9a-z'._ ]", "");
    }

    public static String firstLetterUpperOnly(String text) {
        return text.length() < 1 ?
                text.toUpperCase() : text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
    }
}

package com.vocaby.app.utils;
import com.bugsnag.android.Bugsnag;

public class Logger {
    public static void reportError(Throwable error) {
        Bugsnag.notify(error);
    }
}

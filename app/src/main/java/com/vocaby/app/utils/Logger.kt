package com.vocaby.app.utils;
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.Constants;

public class Logger {
    public static void reportErrorToBugsnag(Throwable error) {
        Bugsnag.notify(error);
    }
    public static void reportErrorToDebug(Throwable error) {
        Log.d(Constants.DEBUG_TAG, error.getMessage());
    }
    public static void reportToDebug(String message) {
        Log.d(Constants.DEBUG_TAG, message);
    }
}

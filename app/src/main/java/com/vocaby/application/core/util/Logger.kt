package com.vocaby.application.core.util

import android.util.Log
import com.bugsnag.android.Bugsnag
import com.vocaby.application.core.Constants

object Logger {
    fun reportErrorToBugsnag(error: Throwable) {
        Bugsnag.notify(error)
    }

    fun reportToDebug(message: String?) {
        message?.let {
            Log.d(Constants.DEBUG_TAG, message)
        } ?: Log.d(Constants.DEBUG_TAG, "null")
    }
}
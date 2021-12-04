package com.vocaby.app.utils

import android.util.Log
import com.bugsnag.android.Bugsnag
import com.vocaby.app.Constants

object Logger {
    fun reportErrorToBugsnag(error: Throwable) {
        Bugsnag.notify(error)
    }

    fun reportErrorToDebug(error: Throwable) {
        error.message?.let { message ->
            Log.d(Constants.DEBUG_TAG, message)
        } ?: Log.d(Constants.DEBUG_TAG, "There was an error...")
    }

    fun reportToDebug(message: String) {
        Log.d(Constants.DEBUG_TAG, message)
    }
}
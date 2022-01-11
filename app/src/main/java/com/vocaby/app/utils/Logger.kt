package com.vocaby.app.utils

import android.util.Log
import com.bugsnag.android.Bugsnag
import com.vocaby.app.Constants

object Logger {
    fun reportErrorToDebug(error: Throwable) {
        error.message?.let { message ->
            Log.d(Constants.DEBUG_TAG, message)
            Log.d(Constants.DEBUG_TAG, error.stackTraceToString())
        } ?: Log.d(Constants.DEBUG_TAG, "There was an error..." + error.javaClass)
    }

    fun reportErrorToBugsnag(error: Throwable) {
        Bugsnag.notify(error)
    }

    fun reportToDebug(message: String?) {
        message?.let {
            Log.d(Constants.DEBUG_TAG, message)
        }
    }
}
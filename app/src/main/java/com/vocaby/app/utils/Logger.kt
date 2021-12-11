package com.vocaby.app.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.bugsnag.android.Bugsnag
import com.vocaby.app.Constants

object Logger {
    fun reportErrorToBugsnag(error: Throwable) {
        Bugsnag.notify(error)
    }

    fun reportErrorToDebug(error: Throwable) {
        error.message?.let { message ->
            Log.d(Constants.DEBUG_TAG, message)
        } ?: Log.d(Constants.DEBUG_TAG, "There was an error..." + error.javaClass)
    }

    fun reportToDebug(message: String?) {
        message?.let {
            Log.d(Constants.DEBUG_TAG, message)
        }
    }
}
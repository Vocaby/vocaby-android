package com.vocaby.application.core.util

import android.util.Log
import com.bugsnag.android.Bugsnag
import com.vocaby.application.core.Constants

object Logger {

    fun reportErrorToBugsnag(error: Throwable) {
        Bugsnag.notify(error)
    }

}
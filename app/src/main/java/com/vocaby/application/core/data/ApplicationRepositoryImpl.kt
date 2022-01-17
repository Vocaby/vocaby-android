package com.vocaby.application.core.data

import android.content.SharedPreferences
import com.vocaby.application.core.Constants
import com.vocaby.application.core.domain.repository.ApplicationRepository

class ApplicationRepositoryImpl(
    private val applicationSharedPref: SharedPreferences
): ApplicationRepository {
    override fun getLastTimeStarted(): Int {
        return applicationSharedPref.getInt(Constants.LAST_APP_STARTED, -1)
    }

    override fun setLastStarted(dayOfYear: Int) {
        applicationSharedPref.edit().putInt(Constants.LAST_APP_STARTED, dayOfYear).apply()
    }
}
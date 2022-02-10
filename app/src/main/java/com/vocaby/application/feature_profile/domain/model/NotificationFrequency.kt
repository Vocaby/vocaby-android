package com.vocaby.application.feature_profile.domain.model

enum class NotificationFrequency(val value: Int, val uiText: String) {
    FIFTEEN_MINUTES(15, "15 Minutes"),
    THIRTY_MINUTES(30, "30 Minutes"),
    ONE_HOUR(60, "1 Hour"),
    SIX_HOURS(360, "6 Hours"),
    TWELVE_HOURS(720, "12 Hours"),
    ONE_DAY(1440, "1 day")
}
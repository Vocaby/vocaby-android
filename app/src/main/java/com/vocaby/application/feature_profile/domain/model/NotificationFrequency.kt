package com.vocaby.application.feature_profile.domain.model

enum class NotificationFrequency(val value: Int, val uiText: String) {
    FIFTEEN_MINUTES(15, "15 minutes"),
    THIRTY_MINUTES(30, "30 minutes"),
    ONE_HOUR(60, "1 hour"),
    TWELVE_HOURS(720, "12 hours"),
    ONE_DAY(1440, "1 day")
}
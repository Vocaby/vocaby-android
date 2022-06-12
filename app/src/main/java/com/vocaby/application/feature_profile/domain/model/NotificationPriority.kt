package com.vocaby.application.feature_profile.domain.model

import android.app.NotificationManager

enum class NotificationPriority(val value: Int, val uiText: String) {
    HIGH(NotificationManager.IMPORTANCE_HIGH, "High"),
    MEDIUM(NotificationManager.IMPORTANCE_DEFAULT, "Medium"),
    LOW(NotificationManager.IMPORTANCE_LOW, "Low")
}
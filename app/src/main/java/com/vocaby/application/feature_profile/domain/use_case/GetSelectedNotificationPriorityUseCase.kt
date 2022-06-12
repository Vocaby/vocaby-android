package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationPriority

class GetSelectedNotificationPriorityUseCase {
    operator fun invoke(minutes: Int): NotificationPriority {
        return when(minutes) {
            NotificationPriority.HIGH.value -> {
                NotificationPriority.HIGH
            }
            NotificationPriority.MEDIUM.value -> {
                NotificationPriority.MEDIUM
            }
            NotificationPriority.LOW.value -> {
                NotificationPriority.LOW
            }
            else -> {
                NotificationPriority.LOW
            }
        }
    }
}
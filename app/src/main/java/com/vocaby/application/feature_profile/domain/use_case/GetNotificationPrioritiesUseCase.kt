package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationPriority
import javax.inject.Inject

class GetNotificationPrioritiesUseCase @Inject constructor(
    private val notificationPriorities: Array<NotificationPriority>
) {
    operator fun invoke(): List<NotificationPriority> {
        return notificationPriorities.toList()
    }
}
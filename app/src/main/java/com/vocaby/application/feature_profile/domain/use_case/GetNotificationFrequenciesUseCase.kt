package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationFrequency
import javax.inject.Inject

class GetNotificationFrequenciesUseCase @Inject constructor(
    private val notificationFrequencies: Array<NotificationFrequency>
) {
    operator fun invoke(): List<NotificationFrequency> {
        return notificationFrequencies.toList()
    }
}
package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationFrequency

class GetSelectedNotificationFrequencyUseCase {
    operator fun invoke(minutes: Int): NotificationFrequency {
        return when(minutes) {
            NotificationFrequency.FIFTEEN_MINUTES.value -> {
                NotificationFrequency.FIFTEEN_MINUTES
            }
            NotificationFrequency.THIRTY_MINUTES.value -> {
                NotificationFrequency.THIRTY_MINUTES
            }
            NotificationFrequency.ONE_HOUR.value -> {
                NotificationFrequency.ONE_HOUR
            }
            NotificationFrequency.TWELVE_HOURS.value -> {
                NotificationFrequency.TWELVE_HOURS
            }
            NotificationFrequency.ONE_DAY.value -> {
                NotificationFrequency.ONE_DAY
            }
            else -> {
                NotificationFrequency.FIFTEEN_MINUTES
            }
        }
    }
}
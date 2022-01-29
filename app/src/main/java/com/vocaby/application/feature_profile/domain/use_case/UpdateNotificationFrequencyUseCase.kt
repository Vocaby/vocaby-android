package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationFrequency
import com.vocaby.application.feature_profile.domain.repository.UserRepository

class UpdateNotificationFrequencyUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(selected:Int, collections: List<NotificationFrequency>?) {
        collections?.let {
            userRepository.setNotificationFrequency(it[selected].value)
        }
    }
}
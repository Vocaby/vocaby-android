package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationPriority
import com.vocaby.application.feature_profile.domain.repository.UserRepository

class UpdateNotificationPriorityUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(selected:Int, collections: List<NotificationPriority>?) {
        collections?.let {
            userRepository.setNotificationPriority(it[selected].value)
        }
    }
}
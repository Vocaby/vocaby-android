package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository

class UpdateNotificationSettingsUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        userRepository.setNotificationSettings(enabled)
    }
}
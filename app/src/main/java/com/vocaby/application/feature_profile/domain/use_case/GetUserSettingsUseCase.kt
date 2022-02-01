package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.app.UserSettings
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<UserSettings> {
        return userRepository.settingsFlow.distinctUntilChanged()
    }
}
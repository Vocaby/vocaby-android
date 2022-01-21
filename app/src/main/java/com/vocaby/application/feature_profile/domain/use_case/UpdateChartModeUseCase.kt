package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository

class UpdateChartModeUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(fetchAllMode: Boolean) {
        userRepository.updateChartMode(fetchAllMode)
    }
}
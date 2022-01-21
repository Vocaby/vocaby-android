package com.vocaby.application.feature_user.domain.use_case

import com.vocaby.application.feature_user.domain.repository.UserRepository

class UpdateChartModeUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(fetchAllMode: Boolean) {
        userRepository.updateChartMode(fetchAllMode)
    }
}
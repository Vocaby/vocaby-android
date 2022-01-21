package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository

class GetChartModeUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Boolean = userRepository.getChartMode()
}
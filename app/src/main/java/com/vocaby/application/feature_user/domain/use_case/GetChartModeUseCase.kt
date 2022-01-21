package com.vocaby.application.feature_user.domain.use_case

import com.vocaby.application.feature_user.domain.repository.UserRepository

class GetChartModeUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Boolean = userRepository.getChartMode()
}
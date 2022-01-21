package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository

class EraseChartDataUseCase(
    val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        userRepository.eraseVisitData(userId)
    }
}
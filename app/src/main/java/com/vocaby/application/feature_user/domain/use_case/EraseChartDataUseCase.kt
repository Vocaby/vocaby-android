package com.vocaby.application.feature_user.domain.use_case

import com.vocaby.application.feature_user.domain.repository.UserRepository

class EraseChartDataUseCase(
    val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        userRepository.eraseVisitData(userId)
    }
}
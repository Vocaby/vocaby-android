package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository

class SetupUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        userRepository.setupUser(1)
    }
}
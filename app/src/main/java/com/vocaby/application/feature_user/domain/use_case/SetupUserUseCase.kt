package com.vocaby.application.feature_user.domain.use_case

import com.vocaby.application.feature_user.domain.repository.UserRepository

class SetupUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        userRepository.setupUser(1)
    }
}
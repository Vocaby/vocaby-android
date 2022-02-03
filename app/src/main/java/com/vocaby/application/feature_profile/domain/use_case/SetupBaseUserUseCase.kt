package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository

class SetupBaseUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Boolean {
        return userRepository.setupBaseUser()
    }
}
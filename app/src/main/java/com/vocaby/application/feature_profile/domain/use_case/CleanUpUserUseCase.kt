package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import javax.inject.Inject

class CleanUpUserUseCase @Inject  constructor(
    val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        userRepository.cleanupUser(userId)
    }
}
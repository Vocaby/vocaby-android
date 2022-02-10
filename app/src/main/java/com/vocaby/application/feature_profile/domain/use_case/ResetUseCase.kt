package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import javax.inject.Inject

class ResetUseCase @Inject  constructor(
    val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        userRepository.createUser()
        userRepository.deleteUser(userId)
    }
}
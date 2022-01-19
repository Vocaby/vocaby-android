package com.vocaby.application.feature_save.domain.use_cases

import com.vocaby.application.feature_user.domain.repository.UserRepository
import javax.inject.Inject

class ClearUserSavesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        userRepository.clearSaves(userId)
    }
}
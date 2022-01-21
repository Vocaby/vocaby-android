package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository

class ClearUserSavesUseCase(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository,
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        saveRepository.clearSaves(userId)
    }
}
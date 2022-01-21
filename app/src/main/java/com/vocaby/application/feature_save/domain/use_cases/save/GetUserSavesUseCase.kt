package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow

class GetUserSavesUseCase(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(): Flow<List<String>> {
        val userId = userRepository.getUser()
        return saveRepository.getAllSavedWordsFlow(userId)
    }

}
package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class CheckSaveUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(entry: String): Flow<Boolean> {
        val userId = userRepository.getUser()
        return saveRepository.hasSaved(userId, entry).transform { emit(it != 0) }
    }
}
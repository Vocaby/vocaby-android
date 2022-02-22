package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckSaveUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    operator fun invoke(userId: Int, entry: String): Flow<SaveModel> {
        return saveRepository.hasSaved(userId, entry)
    }
}
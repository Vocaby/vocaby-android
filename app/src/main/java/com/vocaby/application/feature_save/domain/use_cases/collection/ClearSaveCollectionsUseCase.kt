package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class ClearSaveCollectionsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository,
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        saveRepository.clearSaveCollections(userId)
    }
}
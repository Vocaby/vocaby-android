package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class GetSaveCollectionsForUpdateUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(entry: String): List<UpdateSaveCollectionModel> {
        val userId = userRepository.getUser()

        val collections = saveRepository.getSaveCollectionsForUpdate(userId, entry)

        return collections
    }
}
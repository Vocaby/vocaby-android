package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class GetSelectedNotificationCollectionUseCase @Inject constructor(
    private val saveRepository: SaveRepository,
) {
    suspend operator fun invoke(collectionId: Int): SaveCollectionModel {
        return if (collectionId > 0) {
            saveRepository.getSaveCollectionWithId(collectionId) ?: SaveCollectionModel()
        } else {
            SaveCollectionModel()
        }
    }
}
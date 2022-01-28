package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class RemoveSaveCollectionUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(collectionId: Int) {
        saveRepository.removeSaveCollection(SaveCollection(id = collectionId))
    }
}
package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSaveCollectionsForUpdateUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    operator fun invoke(userId: Int, entry: String): Flow<List<UpdateSaveCollectionModel>> {
        return saveRepository.getSaveCollectionsForUpdate(userId, entry)
    }
}
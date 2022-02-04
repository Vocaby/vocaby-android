package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSaveCollectionItemsUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    operator fun invoke(userId: Int, collectionName: String): Flow<List<String>> {
        return saveRepository.getCollectionItems(userId, collectionName)
    }
}
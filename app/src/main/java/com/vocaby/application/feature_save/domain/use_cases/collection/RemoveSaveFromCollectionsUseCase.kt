package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class RemoveSaveFromCollectionsUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(
        new: List<UpdateSaveCollectionModel>
    ) {
        val itemsToRemove = mutableListOf<SaveCollectionItem>()

        for (item in new) {
            if (!item.saved && item.collectionItemId > -1) {
                itemsToRemove.add(
                    SaveCollectionItem(id=item.collectionItemId)
                )
            }
        }


        saveRepository.removeSaveFromCollections(itemsToRemove)
    }
}
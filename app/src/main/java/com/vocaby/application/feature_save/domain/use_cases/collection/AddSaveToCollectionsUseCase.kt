package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class AddSaveToCollectionsUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(saveId: Int, collections: List<UpdateSaveCollectionModel>) {
        val itemsToAdd = mutableListOf<SaveCollectionItem>()
        for (item in collections) {
            if (item.saved) itemsToAdd.add(SaveCollectionItem(saveId, item.collectionId))
        }

        saveRepository.addSaveToCollections(itemsToAdd)
    }
}
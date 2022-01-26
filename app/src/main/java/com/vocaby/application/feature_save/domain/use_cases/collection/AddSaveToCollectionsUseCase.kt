package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class AddSaveToCollectionsUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(
        saveModel: SaveModel?,
        collections: List<UpdateSaveCollectionModel>?
    ) {
        if (collections != null && saveModel?.saveId != null) {
            val itemsToAdd = mutableListOf<SaveCollectionItem>()
            for (item in collections) {
                if (item.saved && item.collectionItemId == -1) itemsToAdd.add(
                    SaveCollectionItem(saveId = saveModel.saveId, collectionId = item.collectionId)
                )
            }

            saveRepository.addSaveToCollections(itemsToAdd)
        }
    }
}
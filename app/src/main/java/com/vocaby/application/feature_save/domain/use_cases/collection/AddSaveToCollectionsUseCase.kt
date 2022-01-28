package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddSaveToCollectionsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(
        saveModel: SaveModel?,
        collections: List<UpdateSaveCollectionModel>?
    ) = withContext(Dispatchers.Default){
        val userId = userRepository.getUser()
        if (collections != null && saveModel?.saveId != null) {
            val itemsToAdd = mutableListOf<SaveCollectionItem>()
            val collectionsToUpdate = mutableListOf<SaveCollection>()
            for (item in collections) {
                if (item.saved && item.collectionItemId == -1) {
                    itemsToAdd.add(
                        SaveCollectionItem(saveId = saveModel.saveId, collectionId = item.collectionId)
                    )

                    collectionsToUpdate.add(
                        SaveCollection(userId = userId, collectionName = item.name, id = item.collectionId)
                    )
                }
            }

            saveRepository.addSaveToCollections(itemsToAdd)
            saveRepository.updateSaveCollections(collectionsToUpdate)
        }
    }
}
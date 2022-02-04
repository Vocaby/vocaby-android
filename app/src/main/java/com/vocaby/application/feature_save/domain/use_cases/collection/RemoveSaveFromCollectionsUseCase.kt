package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveSaveFromCollectionsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(
        new: List<UpdateSaveCollectionModel>
    ): Boolean = withContext(Dispatchers.Default) {
        val userId = userRepository.getUser()
        val itemsToRemove = mutableListOf<SaveCollectionItem>()
        val collectionsToUpdate = mutableListOf<SaveCollection>()
        for (item in new) {
            if (!item.saved && item.collectionItemId > -1) {
                itemsToRemove.add(
                    SaveCollectionItem(id=item.collectionItemId)
                )

                collectionsToUpdate.add(
                    SaveCollection(userId = userId, collectionName = item.name, id = item.collectionId)
                )
            }
        }

        saveRepository.removeSaveFromCollections(itemsToRemove)
        saveRepository.updateSaveCollections(collectionsToUpdate)
        itemsToRemove.isNotEmpty() || collectionsToUpdate.isNotEmpty()
    }
}
package com.vocaby.application.feature_save.data

import com.vocaby.application.feature_save.data.local.SaveDao
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow

class SaveRepositoryImpl(
    private val saveDao: SaveDao
): SaveRepository {
    override suspend fun getSaveId(userId: Int, entry: String): Int? = saveDao.getSaveId(userId, entry)
    override fun getAllSavesCount(userId: Int): Flow<Int>
        = saveDao.getAllSavesCount(userId)
    override fun getAllSavedEntriesFlow(userId: Int): Flow<List<String>> = saveDao.getSavesFlow(userId)
    override fun hasSaved(userId: Int, entry: String): Flow<SaveModel> = saveDao.hasSave(userId, entry)
    override suspend fun addSaveItem(userSave: UserSave): Long = saveDao.addSave(userSave)
    override suspend fun removeSaveItem(userId: Int, entry: String) = saveDao.removeSave(userId, entry)
    override suspend fun clearSaves(userId: Int) = saveDao.clearSaves(userId)

    override fun getSaveCollections(userId: Int): Flow<List<SaveCollectionModel>> = saveDao.getSaveCollectionsFlow(userId)
    override fun getCollectionItems(userId: Int, collectionName: String): Flow<List<String>> = saveDao.getCollectionItemsFlow(userId, collectionName)
    override fun getSaveCollectionsForUpdate(userId: Int, entry: String): Flow<List<UpdateSaveCollectionModel>> = saveDao.getSaveCollectionsToUpdate(userId, entry)
    override suspend fun addSaveCollection(saveCollection: SaveCollection) = saveDao.addSaveCollection(saveCollection)
    override suspend fun addSaveToCollections(collectionItems: List<SaveCollectionItem>) = saveDao.addSaveToCollections(collectionItems)
    override suspend fun removeSaveCollection(saveCollection: SaveCollection) = saveDao.removeSaveCollection(saveCollection)
    override suspend fun removeSaveFromCollections(collectionItems: List<SaveCollectionItem>) = saveDao.removeSaveFromCollections(collectionItems)
    override suspend fun removeCollectionItem(saveId: Int, collectionId: Int) = saveDao.removeCollectionItem(saveId, collectionId)
}
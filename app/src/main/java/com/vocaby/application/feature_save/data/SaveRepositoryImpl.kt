package com.vocaby.application.feature_save.data

import com.vocaby.application.feature_save.data.local.SaveDao
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow

class SaveRepositoryImpl(
    private val saveDao: SaveDao
): SaveRepository {
    override suspend fun getAllSavesCount(userId: Int): Int
        = saveDao.getAllSavesCount(userId)
    override fun getAllSavedEntriesFlow(userId: Int): Flow<List<String>> = saveDao.getSavesFlow(userId)
    override fun hasSaved(userId: Int, entry: String): Flow<Int> = saveDao.hasSave(userId, entry)
    override suspend fun addSaveItem(userSave: UserSave): Long = saveDao.addSave(userSave)
    override suspend fun removeSaveItem(userId: Int, entry: String) = saveDao.removeSave(userId, entry)
    override suspend fun clearSaves(userId: Int) = saveDao.clearSaves(userId)

    override fun getSaveCollections(userId: Int): Flow<List<SaveCollectionModel>> = saveDao.getSaveCollections(userId)
    override suspend fun addSaveCollection(saveCollection: SaveCollection) = saveDao.addSaveCollection(saveCollection)
}
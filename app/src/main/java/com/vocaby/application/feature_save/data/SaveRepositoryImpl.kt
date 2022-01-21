package com.vocaby.application.feature_save.data

import com.vocaby.application.feature_save.data.local.SaveDao
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow

class SaveRepositoryImpl(
    private val saveDao: SaveDao
): SaveRepository {
    override suspend fun getAllSavesCount(userId: Int): Int
        = saveDao.getAllSavesCount(userId)

    override suspend fun getSaveCollections(userId: Int): List<SaveCollectionModel>
        = saveDao.getUserSaveCollections(userId)

    override suspend fun getSaveId(userId: Int, entry: String) = saveDao.getSave(userId,entry)
    override fun getAllSavedWordsFlow(userId: Int): Flow<List<String>> = saveDao.getSavesFlow(userId)

    override fun hasSaved(userId: Int, entry: String): Flow<Int> = saveDao.hasSave(userId, entry)
    override fun getSavedEntries(userId: Int): Flow<List<String>> {
        TODO("Not yet implemented")
    }
    override suspend fun addSaveItem(userSave: UserSave): Long = saveDao.addSave(userSave)
    override suspend fun removeSaveItem(userSave: UserSave) = saveDao.removeSave(userSave)
    override suspend fun clearSaves(userId: Int) = saveDao.clearSaves(userId)
}
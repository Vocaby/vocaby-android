package com.vocaby.application.feature_save.domain.repository

import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import kotlinx.coroutines.flow.Flow

interface SaveRepository {
    suspend fun getAllSavesCount(userId: Int): Int
    fun getAllSavedEntriesFlow(userId: Int): Flow<List<String>>
    fun hasSaved(userId: Int, entry: String): Flow<Int>
    suspend fun addSaveItem(userSave: UserSave): Long
    suspend fun removeSaveItem(userId: Int, entry: String)
    suspend fun clearSaves(userId: Int)

    fun getSaveCollections(userId: Int): Flow<List<SaveCollectionModel>>
    suspend fun addSaveCollection(saveCollection: SaveCollection)
}
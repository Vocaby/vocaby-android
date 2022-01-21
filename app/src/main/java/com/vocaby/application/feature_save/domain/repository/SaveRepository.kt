package com.vocaby.application.feature_save.domain.repository

import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import kotlinx.coroutines.flow.Flow

interface SaveRepository {
    suspend fun getAllSavesCount(userId: Int): Int
    suspend fun getSaveCollections(userId: Int): List<SaveCollectionModel>
    suspend fun getSaveId(userId: Int, entry: String): Int?
    fun getAllSavedWordsFlow(userId: Int): Flow<List<String>>
    fun hasSaved(userId: Int, entry: String): Flow<Int>
    fun getSavedEntries(userId: Int): Flow<List<String>>
    suspend fun addSaveItem(userSave: UserSave): Long
    suspend fun removeSaveItem(userSave: UserSave)
    suspend fun clearSaves(userId: Int)
}
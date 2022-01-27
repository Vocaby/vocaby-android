package com.vocaby.application.feature_save.domain.repository

import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import kotlinx.coroutines.flow.Flow

interface SaveRepository {
    suspend fun getSaveId(userId: Int, entry: String): Int?
    fun getAllSavesCount(userId: Int): Flow<Int>
    fun getAllSavedEntriesFlow(userId: Int): Flow<List<String>>
    fun hasSaved(userId: Int, entry: String): Flow<SaveModel>
    suspend fun addSaveItem(userSave: UserSave): Long
    suspend fun removeSaveItem(userId: Int, entry: String)
    suspend fun clearSaves(userId: Int)

    fun getSaveCollections(userId: Int): Flow<List<SaveCollectionModel>>
    fun getCollectionItems(userId: Int, collectionName: String): Flow<List<String>>
    fun getSaveCollectionsForUpdate(userId: Int, entry: String): Flow<List<UpdateSaveCollectionModel>>
    suspend fun addSaveCollection(saveCollection: SaveCollection)
    suspend fun addSaveToCollections(collectionItems: List<SaveCollectionItem>)
    suspend fun removeSaveCollection(saveCollection: SaveCollection)
    suspend fun removeSaveFromCollections(collectionItems: List<SaveCollectionItem>)
    suspend fun removeCollectionItem(saveId: Int, collectionId: Int)
}
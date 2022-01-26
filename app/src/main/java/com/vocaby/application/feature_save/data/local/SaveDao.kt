package com.vocaby.application.feature_save.data.local

import androidx.room.*
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveDao {
    @Query("SELECT COUNT(save_id) FROM saves WHERE user_id = :userId")
    fun getAllSavesCount(userId: Int): Flow<Int>

    @Query("SELECT save_id FROM saves s WHERE user_id = :userId AND entry = :entry")
    suspend fun getSaveId(userId: Int, entry: String): Int?

    @Query("SELECT entry FROM saves WHERE user_id = :userId")
    fun getSavesFlow(userId: Int): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSave(userSave: UserSave): Long

    @Query("DELETE FROM saves WHERE user_id = :userId AND entry = :entry")
    suspend fun removeSave(userId: Int, entry: String)

    @Query("WITH s as (SELECT COUNT(save_id), save_id FROM saves WHERE user_id = :userId AND entry = :entry) SELECT save_id AS saveId, save_id IS NOT NULL AS saved FROM s")
    fun hasSave(userId: Int, entry: String): Flow<SaveModel>

    @Query("DELETE FROM saves WHERE user_id = :userId")
    suspend fun clearSaves(userId: Int)

    @Query("SELECT col.collection_id as id, col.collection_name as name, col.last_updated as lastUpdated, " +
            "COUNT(item.collection_item_id) as count " +
            "FROM save_collection col LEFT JOIN save_collection_item item ON col.user_id = :userId " +
            "AND col.collection_id = item.collection_id GROUP BY col.collection_id")
    fun getSaveCollectionsFlow(userId: Int): Flow<List<SaveCollectionModel>>

    @Query("SELECT collectionId, MAX(collectionItemId) AS collectionItemId, name, lastUpdated, MAX(saved) AS saved FROM (SELECT col.collection_id AS collectionId, collection_item_id AS collectionItemId, collection_name AS name, col.last_updated AS lastUpdated, 1 AS saved FROM save_collection col INNER JOIN save_collection_item item ON col.user_id = :userId AND col.collection_id = item.collection_id INNER JOIN saves s ON item.save_id = s.save_id AND entry = :entry UNION ALL SELECT collection_id AS collectionId, -1 AS collectionItemId, collection_name AS name, last_updated AS lastUpdated, 0 AS saved FROM save_collection) GROUP BY collectionId, lastUpdated, name ORDER BY lastUpdated ASC")
    fun getSaveCollectionsToUpdate(userId: Int, entry: String): Flow<List<UpdateSaveCollectionModel>>

    @Query("SELECT entry FROM save_collection_item i INNER JOIN save_collection c ON c.collection_name = :collectionName AND i.collection_id = c.collection_id AND c.user_id = :userId INNER JOIN saves s ON i.save_id = s.save_id")
    fun getCollectionItemsFlow(userId: Int, collectionName: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSaveCollection(collection: SaveCollection)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSaveToCollections(collectionItems: List<SaveCollectionItem>)

    @Delete
    suspend fun removeSaveFromCollections(collectionItems: List<SaveCollectionItem>)

    @Query("DELETE FROM save_collection_item WHERE save_id = :saveId AND collection_id = :collectionId")
    suspend fun removeCollectionItem(saveId: Int, collectionId: Int)
}
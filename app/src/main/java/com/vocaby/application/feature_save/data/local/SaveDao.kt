package com.vocaby.application.feature_save.data.local

import androidx.room.*
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveDao {
    @Query("SELECT COUNT(save_id) FROM saves WHERE user_id = :userId")
    suspend fun getAllSavesCount(userId: Int): Int

    @Query("SELECT save_id FROM saves s WHERE user_id = :userId AND entry = :entry")
    suspend fun getSave(userId: Int, entry: String): Int?

    @Query("SELECT entry FROM saves WHERE user_id = :userId")
    fun getSavesFlow(userId: Int): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSave(userSave: UserSave): Long

    @Query("DELETE FROM saves WHERE user_id = :userId AND entry = :entry")
    suspend fun removeSave(userId: Int, entry: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saves WHERE user_id = :userId AND entry = :entry)")
    fun hasSave(userId: Int, entry: String): Flow<Int>

    @Query("DELETE FROM saves WHERE user_id = :userId")
    suspend fun clearSaves(userId: Int)

    @Query("SELECT col.collection_name as name, col.last_updated as lastUpdated, " +
            "COUNT(item.collection_item_id) as count " +
            "FROM save_collection col LEFT JOIN save_collection_item item ON col.user_id = :userId " +
            "AND col.collection_id = item.collection_id GROUP BY col.collection_id")
    fun getSaveCollections(userId: Int): Flow<List<SaveCollectionModel>>

    @Query("SELECT entry FROM save_collection_item i INNER JOIN save_collection c ON c.collection_name = :collectionName AND i.collection_id = c.collection_id INNER JOIN saves s ON i.save_id = s.save_id")
    fun getCollectionSavesFlow(collectionName: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSaveCollection(collection: SaveCollection)
}
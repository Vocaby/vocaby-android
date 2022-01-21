package com.vocaby.application.feature_save.data.local

import androidx.room.*
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveDao {
    @Query("SELECT COUNT(save_id) FROM saves WHERE user_id = :userId")
    suspend fun getAllSavesCount(userId: Int): Int

    @Query("SELECT col.collection_name as name, col.last_updated as lastUpdated, " +
            "COUNT(item.collection_item_id) as count " +
            "FROM save_collection col LEFT JOIN save_collection_item item ON col.user_id = :userId " +
            "AND col.collection_id = item.collection_id GROUP BY col.collection_id")
    suspend fun getUserSaveCollections(userId: Int): List<SaveCollectionModel>

    @Query("SELECT s.save_id FROM saves s INNER JOIN dictionary_word d ON d.id = s.entry_id " +
            "AND d.word = :entry AND s.user_id = :userId UNION " +
            "SELECT s.save_id FROM saves s INNER JOIN custom_user_entry c " +
            "ON c.custom_entry_id = s.custom_entry_id AND c.entry = :entry AND s.user_id = :userId")
    suspend fun getSave(userId: Int, entry: String): Int?

    @Query("SELECT d.word FROM saves s INNER JOIN dictionary_word d ON d.id = s.entry_id AND s.user_id = :userId UNION SELECT c.entry FROM saves s INNER JOIN custom_user_entry c ON c.custom_entry_id = s.custom_entry_id AND s.user_id = :userId")
    fun getSavesFlow(userId: Int): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSave(userSave: UserSave): Long

    @Delete
    suspend fun removeSave(userSave: UserSave)

    @Query("SELECT EXISTS(SELECT 1 FROM saves s INNER JOIN dictionary_word d " +
            "ON d.id = s.entry_id AND d.word = :entry AND s.user_id = :userId " +
            "UNION SELECT 1 FROM saves s INNER JOIN custom_user_entry c " +
            "ON c.custom_entry_id = s.custom_entry_id AND c.entry = :entry AND s.user_id = :userId)")
    fun hasSave(userId: Int, entry: String): Flow<Int>

    @Query("DELETE FROM saves WHERE user_id = :userId")
    suspend fun clearSaves(userId: Int)
}
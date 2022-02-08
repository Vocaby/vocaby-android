package com.vocaby.application.feature_dictionary_custom.data.local

import androidx.room.*
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomDefinition
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntryGroup
import com.vocaby.application.feature_dictionary_custom.data.local.entity.EntryWithData
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomDictionaryDao {
    @Transaction
    @Query("UPDATE custom_user_entry SET user_id = :newUserId")
    suspend fun replaceUser(newUserId: Int)

    @Transaction
    @Query("SELECT * FROM custom_user_entry WHERE user_id = :userId AND entry = :entry")
    suspend fun getUserEntryData(userId: Int, entry: String?): EntryWithData?

    @Query("SELECT custom_entry_id FROM custom_user_entry WHERE user_id = :userId AND entry = :entry")
    suspend fun getUserEntryId(userId: Int, entry: String?): Int?

    @Query(
        "SELECT entry, last_updated FROM custom_user_entry WHERE user_id = :userId " +
                "AND entry LIKE :prefix||'%' ORDER BY last_updated DESC"
    )
    suspend fun filterUserEntries(userId: Int, prefix: String): List<UserEntry>

    @Transaction
    @Query("SELECT * FROM custom_user_entry WHERE user_id = :userId")
    suspend fun getAllUserEntryData(userId: Int): List<EntryWithData>

    @Query("DELETE FROM custom_user_entry WHERE entry = :entry")
    suspend fun deleteUserEntry(entry: String)

    @Query("DELETE FROM custom_user_entry WHERE custom_entry_id = :entryId")
    suspend fun deleteUserEntry(entryId: Int)

    @Query("SELECT entry, last_updated FROM custom_user_entry WHERE user_id = :id ORDER BY last_updated DESC")
    suspend fun getUserEntries(id: Int): List<UserEntry>

    @Query("DELETE FROM custom_user_entry WHERE user_id = :id")
    suspend fun clearUserEntries(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomEntry(customEntry: CustomEntry): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomEntries(customEntries: List<CustomEntry>): List<Long>

    @Update
    suspend fun updateCustomEntry(customEntry: CustomEntry)

    @Insert
    suspend fun insertCustomEntryGroups(customEntryGroups: List<CustomEntryGroup>): List<Long>

    @Delete
    suspend fun deleteCustomEntryGroups(customEntryGroups: List<CustomEntryGroup>)

    @Update
    suspend fun updateCustomEntryGroups(customEntryGroups: List<CustomEntryGroup>)

    @Insert
    suspend fun insertCustomDefinitions(customDefinitions: List<CustomDefinition>)
    @Delete
    suspend fun deleteCustomDefinitions(customDefinitions: List<CustomDefinition>)

    @Update
    suspend fun updateCustomDefinitions(customDefinitions: List<CustomDefinition>)

    /** --------------------- TYPES -------------------- **/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTypes(vararg types: Type)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTypes(types: List<Type>)

    @Delete
    suspend fun removeTypes(types: List<Type>)

    @Update
    suspend fun updateTypes(types: List<Type>)

    @Query("SELECT * from entry_type ORDER BY `order` ASC")
    fun getTypes(): Flow<List<Type>>

    @Query("DELETE FROM entry_type")
    suspend fun clearTypes()
}
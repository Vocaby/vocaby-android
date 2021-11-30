package com.vocaby.app.data.dao

import androidx.room.*
import com.vocaby.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabyDaoKt {
    /** -------------------- USER -------------------- **/
    @Query("SELECT EXISTS(SELECT * FROM vocaby_user WHERE user_id = :id)")
    suspend fun checkUser(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createUser(user: User): Long

    /** --------------------- ENTRY -------------------- **/
    @Query("SELECT EXISTS(SELECT 1 FROM dictionary_word WHERE word = :entry)")
    suspend fun checkEntryExistence(entry: String): Boolean

    @Query(
        "SELECT word FROM dictionary_word UNION " +
                "SELECT entry FROM custom_user_entry " +
                "ORDER BY word COLLATE NOCASE ASC"
    )
    suspend fun getDictionaryEntries(): List<String>

    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE word = :entry")
    suspend fun getEntryData(entry: String): WordDefinitions?

    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE id = :entryId")
    suspend fun getEntryDataWithId(entryId: Int): WordDefinitions?

    /** --------------------- CUSTOM ENTRY -------------------- **/
    @Transaction
    @Query("SELECT * FROM custom_user_entry WHERE user_id = :userId AND entry = :entry")
    suspend fun getUserEntryData(userId: Int, entry: String?): EntryWithData?

    @Transaction
    @Query(
        "SELECT * FROM dictionary_word WHERE id = " +
                "(SELECT id FROM dictionary_word ORDER BY RANDOM() LIMIT 1)"
    )
    suspend fun getRandomWord(): WordDefinitions

    @Query("DELETE FROM custom_user_entry WHERE entry = :entry")
    suspend fun deleteUserEntry(entry: String)

    @Query("DELETE FROM custom_user_entry WHERE custom_entry_id = :entryId")
    suspend fun deleteUserEntry(entryId: Int)

    @Query("SELECT entry FROM custom_user_entry WHERE user_id = :id ORDER BY last_updated DESC")
    suspend fun getUserEntries(id: Int): List<String>

    @Query("DELETE FROM custom_user_entry WHERE user_id = :id")
    suspend fun clearUserEntries(id: Int)

    @Insert
    suspend fun insertCustomEntry(customEntry: CustomEntry): Long

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

    /** --------------------- SAVES -------------------- **/
    @Query("SELECT entry FROM saves WHERE user_id = :userId ORDER BY id DESC")
    fun getSaves(userId: Int): Flow<MutableList<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSave(userSave: UserSave)

    @Query("DELETE FROM saves WHERE user_id = :userId AND entry = :entry")
    suspend fun removeSave(userId: Int, entry: String)

    @Query("DELETE FROM saves WHERE user_id = :userId")
    suspend fun clearSaves(userId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saves WHERE entry = :entry AND user_id = :userId)")
    fun hasSave(userId: Int, entry: String): Flow<Int>

    /** --------------------- TYPES -------------------- **/
    @Insert
    fun insertTypes(vararg types: Type)

    @Query("SELECT type from entry_type")
    suspend fun getTypes(): List<String>
}
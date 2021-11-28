package com.vocaby.app.data.dao

import androidx.room.*
import com.vocaby.app.data.entity.*
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabyDaoKt {
    /** -------------------- USER -------------------- **/
    @Query("SELECT EXISTS(SELECT * FROM vocaby_user WHERE user_id = :id)")
    suspend fun checkUser(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createUser(user: User): Long

    /** --------------------- ENTRY -------------------- **/
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

    @Insert
    fun insertTypes(vararg types: Type)
}
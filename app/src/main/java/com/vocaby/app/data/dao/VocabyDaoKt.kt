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
    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE word = :entry")
    suspend fun getEntryData(entry: String): WordDefinitions?

    /** --------------------- CUSTOM ENTRY -------------------- **/
    @Transaction
    @Query("SELECT * FROM custom_user_entry WHERE user_id = :userId AND entry = :entry")
    suspend fun getUserEntryData(userId: Int, entry: String?): EntryWithData?

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
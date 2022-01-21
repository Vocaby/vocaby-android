package com.vocaby.application.feature_profile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vocaby.application.feature_profile.data.local.entity.CustomDictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.DictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.User
import com.vocaby.application.feature_profile.domain.model.VisitData

@Dao
interface UserDao {
    @Query("SELECT EXISTS(SELECT * FROM vocaby_user WHERE user_id = :id)")
    suspend fun checkUser(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: User): Long

    /** --------------------- SAVES -------------------- **/
//    @Query("SELECT entry_id FROM saves WHERE user_id = :userId ORDER BY save_id DESC")
//    fun getSavesFlow(userId: Int)
//
//    @Query("SELECT entry_id FROM saves WHERE user_id = :userId ORDER BY save_id ASC")
//    suspend fun getSaves(userId: Int)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun addSave(userSave: UserSave)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun addSaves(userSaves: List<UserSave>)
//
//    @Query("DELETE FROM saves WHERE save_id = :saveId")
//    suspend fun removeSave(saveId: Int)
//
//    @Query("DELETE FROM saves WHERE user_id = :userId")
//    suspend fun clearSaves(userId: Int)
//
//    @Query("SELECT EXISTS(SELECT 1 FROM saves)")
//    fun hasSave(userId: Int, entry: String): Flow<Int>

    /** --------------------- DATA -------------------- **/
    @Insert
    suspend fun recordVisit(dictionaryViewCount: DictionaryViewCount)

    @Insert
    suspend fun recordCustomVisit(customDictionaryViewCount: CustomDictionaryViewCount)

    @Query("SELECT word as entry, COUNT(entry_id) AS count FROM dictionary_view_count " +
            "JOIN dictionary_word ON dictionary_view_count.entry_id = dictionary_word.id GROUP BY entry_id " +
            "UNION " +
            "SELECT entry, COUNT(custom_dictionary_view_count.custom_entry_id) AS count FROM custom_dictionary_view_count " +
            "JOIN custom_user_entry ON custom_dictionary_view_count.custom_entry_id = custom_user_entry.custom_entry_id " +
            "GROUP BY custom_dictionary_view_count.custom_entry_id " +
            "ORDER BY count DESC " +
            "LIMIT :size")
    suspend fun getAllSearchData(size: Int): List<VisitData>

    @Query("SELECT DATE('now', 'start of month')")
    suspend fun getDate(): String

    @Query("SELECT word as entry, COUNT(entry_id) AS count FROM dictionary_view_count " +
            "JOIN dictionary_word ON dictionary_view_count.entry_id = dictionary_word.id " +
            "WHERE DATE(date_visited) BETWEEN DATE('now', 'localtime', 'start of month') AND DATE('now', 'localtime') " +
            "GROUP BY entry_id " +
            "UNION " +
            "SELECT entry, COUNT(custom_dictionary_view_count.custom_entry_id) AS count FROM custom_dictionary_view_count " +
            "JOIN custom_user_entry ON custom_dictionary_view_count.custom_entry_id = custom_user_entry.custom_entry_id " +
            "WHERE DATE(date_visited) BETWEEN DATE('now', 'localtime', 'start of month') AND DATE('now', 'localtime') " +
            "GROUP BY custom_dictionary_view_count.custom_entry_id " +
            "ORDER BY count DESC " +
            "LIMIT :size")
    suspend fun getMonthlySearchData(size: Int): List<VisitData>

    @Query("DELETE FROM dictionary_view_count WHERE user_id = :userId")
    suspend fun deleteVisit(userId: Int)

    @Query("DELETE FROM custom_dictionary_view_count WHERE user_id = :userId")
    suspend fun deleteCustomVisit(userId: Int)
}
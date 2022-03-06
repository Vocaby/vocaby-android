package com.vocaby.application.feature_profile.data.local

import androidx.room.*
import com.vocaby.application.feature_profile.data.local.entity.CustomDictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.DictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.User
import com.vocaby.application.feature_profile.domain.model.ProfileModel
import com.vocaby.application.feature_profile.domain.model.VisitData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT user_id FROM vocaby_user ORDER BY user_id ASC LIMIT 1")
    fun getCurrentUserFlow(): Flow<Long?>

    @Query("SELECT user_id FROM vocaby_user ORDER BY user_id ASC LIMIT 1")
    suspend fun getCurrentUser(): Long?

    @Query("SELECT EXISTS(SELECT 1 FROM vocaby_user WHERE user_id = :id)")
    suspend fun checkUserExists(id: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM vocaby_user)")
    suspend fun checkAnyUserExists(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: User): Long

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM vocaby_user WHERE user_id != :userId")
    suspend fun cleanupUser(userId: Int)

    @Query("SELECT u.user_id as userId, u.email, u.firstName, u.lastName, (SELECT COUNT(save_id) FROM saves WHERE user_id = :userId) as saveCount," +
            "(SELECT COUNT(collection_id) FROM save_collection WHERE user_id = :userId) as collectionCount, " +
            "(SELECT COUNT(custom_entry_id) FROM custom_user_entry WHERE user_id = :userId) as entryCount " +
            "FROM vocaby_user u WHERE user_id = :userId")
    fun getProfileData(userId: Int): Flow<ProfileModel?>

    /** --------------------- DATA -------------------- **/
    @Query("UPDATE custom_user_entry SET user_id = :newUserId")
    suspend fun replaceCustomDictionaryUser(newUserId: Int)

    @Query("UPDATE custom_dictionary_view_count SET user_id = :newUserId")
    suspend fun replaceDictionaryVisitUser(newUserId: Int)

    @Query("UPDATE dictionary_view_count SET user_id = :newUserId")
    suspend fun replaceCustomDictionaryVisitUser(newUserId: Int)

    @Insert
    suspend fun recordVisit(dictionaryViewCount: DictionaryViewCount)

    @Insert
    suspend fun recordCustomVisit(customDictionaryViewCount: CustomDictionaryViewCount)

    @Query("SELECT entry, COUNT(entry_id) AS count FROM dictionary_view_count " +
            "JOIN dictionary_entry ON dictionary_view_count.entry_id = dictionary_entry.id GROUP BY entry_id " +
            "UNION " +
            "SELECT entry, COUNT(custom_dictionary_view_count.custom_entry_id) AS count FROM custom_dictionary_view_count " +
            "JOIN custom_user_entry ON custom_dictionary_view_count.custom_entry_id = custom_user_entry.custom_entry_id " +
            "GROUP BY custom_dictionary_view_count.custom_entry_id " +
            "ORDER BY count DESC " +
            "LIMIT :size")
    fun getAllSearchData(size: Int): Flow<List<VisitData>>

    @Query("SELECT entry, COUNT(entry_id) AS count FROM dictionary_view_count " +
            "JOIN dictionary_entry ON dictionary_view_count.entry_id = dictionary_entry.id " +
            "WHERE DATE(date_visited) BETWEEN DATE('now', 'localtime', 'start of month') AND DATE('now', 'localtime') " +
            "GROUP BY entry_id " +
            "UNION " +
            "SELECT entry, COUNT(custom_dictionary_view_count.custom_entry_id) AS count FROM custom_dictionary_view_count " +
            "JOIN custom_user_entry ON custom_dictionary_view_count.custom_entry_id = custom_user_entry.custom_entry_id " +
            "WHERE DATE(date_visited) BETWEEN DATE('now', 'localtime', 'start of month') AND DATE('now', 'localtime') " +
            "GROUP BY custom_dictionary_view_count.custom_entry_id " +
            "ORDER BY count DESC " +
            "LIMIT :size")
    fun getMonthlySearchData(size: Int): Flow<List<VisitData>>

    @Query("DELETE FROM dictionary_view_count WHERE user_id = :userId")
    suspend fun deleteVisit(userId: Int)

    @Query("DELETE FROM custom_dictionary_view_count WHERE user_id = :userId")
    suspend fun deleteCustomVisit(userId: Int)
}
package com.vocaby.application.data.dao

import androidx.room.*
import com.vocaby.application.data.entity.*
import com.vocaby.application.models.customentry.UserEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabyDao {
    /** -------------------- USER -------------------- **/
    @Query("SELECT EXISTS(SELECT * FROM vocaby_user WHERE user_id = :id)")
    suspend fun checkUser(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: User): Long

    /** --------------------- ENTRY -------------------- **/
    @Delete
    suspend fun deleteEntry(word: Word)

    @Insert
    suspend fun insertEntry(word: Word): Long

    @Insert
    suspend fun insertDefinitions(wordDefinitions: List<Definition>)

    @Query("SELECT EXISTS(SELECT 1 FROM dictionary_word WHERE word = :entry)")
    suspend fun checkEntryExistence(entry: String): Boolean

    @Query(
        "SELECT word FROM dictionary_word WHERE word LIKE :firstLetter||'%' UNION " +
                "SELECT entry FROM custom_user_entry WHERE entry LIKE :firstLetter||'%' " +
                "ORDER BY word ASC"
    )
    suspend fun getDictionaryEntriesByCharacter(firstLetter: String): List<String>

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
    @Query("SELECT * FROM custom_user_entry WHERE user_id = :userId")
    suspend fun getAllUserEntryData(userId: Int): List<EntryWithData>

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

    /** --------------------- SAVES -------------------- **/
    @Query("SELECT entry FROM saves WHERE user_id = :userId ORDER BY id DESC")
    fun getSavesFlow(userId: Int): Flow<MutableList<String>>

    @Query("SELECT entry FROM saves WHERE user_id = :userId ORDER BY id ASC")
    suspend fun getSaves(userId: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSave(userSave: UserSave)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSaves(userSaves: List<UserSave>)

    @Query("DELETE FROM saves WHERE user_id = :userId AND entry = :entry")
    suspend fun removeSave(userId: Int, entry: String)

    @Query("DELETE FROM saves WHERE user_id = :userId")
    suspend fun clearSaves(userId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saves WHERE entry = :entry AND user_id = :userId)")
    fun hasSave(userId: Int, entry: String): Flow<Int>

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
            "WHERE DATE(date_visited) BETWEEN DATE('now', 'start of month') AND DATE('now') " +
            "GROUP BY entry_id " +
            "UNION " +
            "SELECT entry, COUNT(custom_dictionary_view_count.custom_entry_id) AS count FROM custom_dictionary_view_count " +
            "JOIN custom_user_entry ON custom_dictionary_view_count.custom_entry_id = custom_user_entry.custom_entry_id " +
            "WHERE DATE(date_visited) BETWEEN DATE('now', 'start of month') AND DATE('now') " +
            "GROUP BY custom_dictionary_view_count.custom_entry_id " +
            "ORDER BY count DESC " +
            "LIMIT :size")
    suspend fun getMonthlySearchData(size: Int): List<VisitData>

    @Query("DELETE FROM dictionary_view_count WHERE user_id = :userId")
    suspend fun deleteVisit(userId: Int)

    @Query("DELETE FROM custom_dictionary_view_count WHERE user_id = :userId")
    suspend fun deleteCustomVisit(userId: Int)

    /** --------------------- TYPES -------------------- **/
    @Insert
    fun insertTypes(vararg types: Type)

    @Query("SELECT type from entry_type")
    suspend fun getTypes(): List<String>
}
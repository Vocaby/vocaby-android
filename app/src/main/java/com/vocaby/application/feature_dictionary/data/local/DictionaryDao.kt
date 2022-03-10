package com.vocaby.application.feature_dictionary.data.local

import androidx.room.*
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Entry
import com.vocaby.application.feature_dictionary.data.local.entity.EntryDefinitions

@Dao
interface DictionaryDao {
    @Delete
    suspend fun deleteEntry(entry: Entry)

    @Insert
    suspend fun insertEntry(entry: Entry): Long

    @Insert
    suspend fun insertEntries(entries: List<Entry>)

    @Insert
    suspend fun insertDefinitions(wordDefinitions: List<Definition>)

    @Query("SELECT EXISTS(SELECT 1 FROM dictionary_entry WHERE entry = :entry)")
    suspend fun checkEntryExistence(entry: String): Boolean

    @Query("SELECT id FROM dictionary_entry WHERE entry = :entry")
    suspend fun getEntryId(entry: String): Long?

    @Query(
        "SELECT entry FROM dictionary_entry WHERE entry LIKE :firstLetter||'%' UNION " +
                "SELECT entry FROM custom_user_entry WHERE entry LIKE :firstLetter||'%' " +
                "ORDER BY entry ASC"
    )
    suspend fun getDictionaryEntriesByCharacter(firstLetter: String): List<String>

    @Transaction
    @Query("SELECT * FROM dictionary_entry WHERE entry = :entry")
    suspend fun getEntryData(entry: String): EntryDefinitions?

    @Transaction
    @Query("SELECT * FROM dictionary_entry WHERE id = :entryId")
    suspend fun getEntryDataWithId(entryId: Int): EntryDefinitions?

    @Transaction
    @Query(
        "SELECT * FROM dictionary_entry ode WHERE id = " +
                "(SELECT de.id FROM dictionary_entry de INNER JOIN dictionary_definition dd " +
                "ON de.id = dd.entry_id AND LENGTH(de.entry) < 16  AND LENGTH(de.entry) > 5 AND dd.example != '' " +
                "ORDER BY RANDOM() LIMIT 1)"
    )
    suspend fun getRandomWord(): EntryDefinitions
}
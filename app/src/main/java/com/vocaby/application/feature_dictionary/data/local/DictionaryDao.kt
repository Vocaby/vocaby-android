package com.vocaby.application.feature_dictionary.data.local

import androidx.room.*
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_dictionary.data.local.entity.WordDefinitions

@Dao
interface DictionaryDao {
    @Delete
    suspend fun deleteEntry(word: Word)

    @Insert
    suspend fun insertEntry(word: Word): Long

    @Insert
    suspend fun insertEntries(words: List<Word>)

    @Insert
    suspend fun insertDefinitions(wordDefinitions: List<Definition>)

    @Query("SELECT EXISTS(SELECT 1 FROM dictionary_word WHERE word = :entry)")
    suspend fun checkEntryExistence(entry: String): Boolean

    @Query("SELECT id FROM dictionary_word WHERE word = :entry")
    suspend fun getEntryId(entry: String): Long?

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

    @Transaction
    @Query(
        "SELECT * FROM dictionary_word WHERE id = " +
                "(SELECT id FROM dictionary_word ORDER BY RANDOM() LIMIT 1)"
    )
    suspend fun getRandomWord(): WordDefinitions
}
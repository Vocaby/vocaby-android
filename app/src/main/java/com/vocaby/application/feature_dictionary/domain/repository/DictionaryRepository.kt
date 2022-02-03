package com.vocaby.application.feature_dictionary.domain.repository

import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import java.util.*

interface DictionaryRepository {
    suspend fun getEntryDataFromDatabase(entry: String): EntryModel?
    suspend fun getEntryIdFromDatabase(entry: String): Long?
    suspend fun getEntriesByCharacterFromDB(character: String): List<String>
    suspend fun getRandomEntry(): EntryModel
    suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int
    suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel?
    suspend fun checkApiCache(entry: String): Boolean
    suspend fun clearDictionaryCache()

    suspend fun getDailyPickFromApi(): EntryModel?
    suspend fun cacheDailyPick(entry: String, random: Boolean)
    suspend fun getCachedPick(): Pair<String, Boolean>

    /** --------------------- HISTORY -------------------- **/
    fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>?
    fun getHistory(): LinkedList<SimpleEntryModel>?
    fun getHistory(position: Int): String?
    fun clearHistory(): List<SimpleEntryModel>?
}
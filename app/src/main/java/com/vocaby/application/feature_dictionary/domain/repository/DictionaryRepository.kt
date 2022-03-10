package com.vocaby.application.feature_dictionary.domain.repository

import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import kotlinx.coroutines.flow.Flow
import java.util.*

interface DictionaryRepository {
    suspend fun getEntryDataFromDatabase(entry: String): EntryModel?
    suspend fun getEntryIdFromDatabase(entry: String): Long?
    suspend fun getEntriesByCharacterFromDB(character: String): List<String>
    suspend fun getRandomEntry(): EntryModel
    suspend fun replaceEntry(originalId: Int?, remote: EntryModel): Int
    suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel?
    suspend fun checkApiCache(entry: String): Boolean
    suspend fun clearDictionaryApiCache()
    suspend fun clearDailyPickCache()

    suspend fun getDailyPickFromApi(): EntryModel?
    suspend fun cacheDailyPick(apiPick: String, randomPick: String)
    suspend fun cachePrevPick(apiPick: String, randomPick: String)
    fun getPrevPick(): Flow<Pair<String, String>>
    suspend fun getCachedPick(): Pair<String, String>

    /** --------------------- HISTORY -------------------- **/
    fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>?
    fun getHistory(): LinkedList<SimpleEntryModel>?
    fun getHistory(position: Int): String?
    fun clearHistory(): List<SimpleEntryModel>?
}
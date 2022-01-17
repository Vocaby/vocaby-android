package com.vocaby.application.feature_dictionary.domain.repository

import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import java.util.*

interface DictionaryRepository {
    suspend fun getEntryDataFromDatabase(entry: String): EntryModel?
    suspend fun getEntriesByCharacterFromDB(character: String): List<String>
    suspend fun getRandomEntry(): EntryModel
    suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int
    suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel?
    fun checkApiCache(entry: String): Boolean
    fun clearDictionaryCache()

    suspend fun getDailyPickFromApi(): EntryModel?
    fun cacheDailyPick(entry: String, random: Boolean)
    fun getCachedPick(): DailyPick

    /** --------------------- HISTORY -------------------- **/
    fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>?
    fun getHistory(): LinkedList<SimpleEntryModel>?
    fun clearHistory(): List<SimpleEntryModel>?
}
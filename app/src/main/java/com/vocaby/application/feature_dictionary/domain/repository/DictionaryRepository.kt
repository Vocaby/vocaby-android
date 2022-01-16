package com.vocaby.application.feature_dictionary.domain.repository

import com.vocaby.application.feature_dictionary.data.local.entity.WordDefinitions
import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel

interface DictionaryRepository {
    suspend fun getEntryDataFromDatabase(entry: String): EntryModel?
    fun convertToEntryModel(wordDefinitions: WordDefinitions?): EntryModel?
    suspend fun getEntriesByCharacterFromDB(character: String): List<String>
    suspend fun getDailyPick(): DailyPick
    suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int
    suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel?
    fun checkApiCache(entry: String): Boolean
    fun clearDictionaryCache()
}
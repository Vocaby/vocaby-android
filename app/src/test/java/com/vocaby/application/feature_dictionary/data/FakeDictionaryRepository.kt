package com.vocaby.application.feature_dictionary.data

import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import java.util.*

class FakeDictionaryRepository: DictionaryRepository {
    override suspend fun getEntryDataFromDatabase(entry: String): EntryModel? {
        TODO("Not yet implemented")
    }

    override suspend fun getEntryIdFromDatabase(entry: String): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun getEntriesByCharacterFromDB(character: String): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getRandomEntry(): EntryModel {
        TODO("Not yet implemented")
    }

    override suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int {
        TODO("Not yet implemented")
    }

    override suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel? {
        TODO("Not yet implemented")
    }

    override suspend fun checkApiCache(entry: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun clearDictionaryCache() {
        TODO("Not yet implemented")
    }

    override suspend fun getDailyPickFromApi(): EntryModel? {
        TODO("Not yet implemented")
    }

    override suspend fun cacheDailyPick(entry: String, random: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun getCachedPick(): Pair<String, Boolean> {
        TODO("Not yet implemented")
    }

    override fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>? {
        TODO("Not yet implemented")
    }

    override fun getHistory(): LinkedList<SimpleEntryModel>? {
        TODO("Not yet implemented")
    }

    override fun getHistory(position: Int): String? {
        TODO("Not yet implemented")
    }

    override fun clearHistory(): List<SimpleEntryModel>? {
        TODO("Not yet implemented")
    }
}
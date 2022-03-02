package com.vocaby.application.feature_dictionary

import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import java.util.*

class FakeDictionaryRepository: DictionaryRepository {
    private val wordData = mutableListOf(
        Word("enthusiasm", "", Date()),
        Word("creativity", "", Date()),
        Word("sacred", "", Date()),
        Word("galvanize", "", Date()),
        Word("fastidious", "", Date()),
        Word("sage", "", Date()),
    )

    private val definitionData = mutableListOf(
        Definition(1, "intense and eager enjoyment, interest, or approval.", "\"her energy and enthusiasm for life\"", "noun"),
        Definition(2, "the use of the imagination or original ideas, especially in the production of an artistic work.", "\"firms are keen to encourage creativity\"", "noun"),
        Definition(3, "connected with God (or the gods) or dedicated to a religious purpose and so deserving veneration.", "", "noun"),
        Definition(4, "shock or excite (someone) into taking action.", "\"the urgency of his voice galvanized them into action\"", "verb"),
        Definition(4, "coat (iron or steel) with a protective layer of zinc.", "\"they promised they would galvanize the iron railings to prevent rusting\"", "verb"),
        Definition(5, "very attentive to and concerned about accuracy and detail.", "\"he chooses his words with fastidious care\"", "adjective"),
        Definition(6, "a profoundly wise man, especially one who features in ancient history or legend.", "\"the sayings of the numerous venerable sages\"", "adjective"),
    )

    override suspend fun getEntryDataFromDatabase(entry: String): EntryModel? {
        return null
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

    override suspend fun replaceEntry(originalId: Int?, remote: EntryModel): Int {
        TODO("Not yet implemented")
    }

    override suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel? {
        TODO("Not yet implemented")
    }

    override suspend fun checkApiCache(entry: String): Boolean {
        return true
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
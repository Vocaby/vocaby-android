package com.vocaby.application.feature_dictionary.data

import android.content.SharedPreferences
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.data.local.DictionaryDao
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_dictionary.data.local.entity.WordDefinitions
import com.vocaby.application.feature_dictionary.data.remote.DictionaryApi
import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import java.util.*

class DictionaryRepositoryImpl(
    private val dao: DictionaryDao,
    private val api: DictionaryApi,
    private val applicationSharedPref: SharedPreferences,
    private val cacheSharedPref: SharedPreferences,

    ): DictionaryRepository {
    override suspend fun getEntryDataFromDatabase(entry: String): EntryModel?
        = convertToEntryModel(dao.getEntryData(entry))

    override fun convertToEntryModel(wordDefinitions: WordDefinitions?): EntryModel? {
        wordDefinitions?.let {
            val pronunciation =
                wordDefinitions.wordData.pronunciation?.let { wordDefinitions.wordData.pronunciation }
                    ?: ""

            val wordData = EntryModel(
                wordDefinitions.wordData.id,
                wordDefinitions.wordData.word,
                pronunciation,
                wordDefinitions.wordData.lastUpdated
            )

            for (data in wordDefinitions.definitions) {
                wordData.addDefinition(data.pos, data.definition, data.sentence)
            }

            return wordData
        }

        return null
    }

    override suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int {
        dao.deleteEntry(Word(original.id))
        val id = dao.insertEntry(Word(remote.entry, remote.pronunciation, remote.lastUpdated)).toInt()
        val definitions = mutableListOf<Definition>()
        for(groupData in remote.definitionGroups) {
            for (definitionData in groupData.definitionData) {
                definitions.add(
                    Definition(
                        id,
                        definitionData.definition,
                        definitionData.example,
                        groupData.type)
                )
            }
        }

        dao.insertDefinitions(definitions)
        return id
    }

    override suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel? {
        return try {
            val response = api.checkAndGetDefinitions(entry, date)

            val oldSet = cacheSharedPref.getStringSet(Constants.SEARCH_CACHE, mutableSetOf<String>())!!
            val newSet = oldSet.toMutableSet()
            newSet.add(entry)
            cacheSharedPref.edit().putStringSet(Constants.SEARCH_CACHE, newSet).apply()

            response.body()
        } catch (throwable: Throwable) {
            null
        }
    }

    override fun checkApiCache(entry: String): Boolean {
        val set = cacheSharedPref.getStringSet(Constants.SEARCH_CACHE, mutableSetOf<String>())!!
        return set.contains(entry)
    }

    override fun clearDictionaryCache() {
        cacheSharedPref.edit().clear().apply()
    }

    override suspend fun getEntriesByCharacterFromDB(character: String): List<String>
        = dao.getDictionaryEntriesByCharacter(character)

    override suspend fun getDailyPick(): DailyPick {
        val editor = applicationSharedPref.edit()
        val lastTimeStarted = applicationSharedPref.getInt(Constants.LAST_APP_STARTED, -1)
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]
        val dailyPick: DailyPick

        if (today != lastTimeStarted) {
            dailyPick = try {
                val response = api.getWoD(Formatter.formatDateToString(Date().time, precise=false))
                if (response.isSuccessful && response.code() == 200) {
                    DailyPick(response.body(), false)
                } else {
                    val data: WordDefinitions = dao.getRandomWord()
                    DailyPick(convertToEntryModel(data), true)
                }
            } catch (throwable: Throwable) {
                val data: WordDefinitions = dao.getRandomWord()
                DailyPick(convertToEntryModel(data), true)
            }

            dailyPick.entryModel?.let { it ->
                editor.putString(Constants.DICTIONARY_PICK_ID, it.entry)
                editor.putInt(Constants.LAST_APP_STARTED, today)
                editor.putBoolean(Constants.DICTIONARY_PICK_RANDOM, dailyPick.random)
                editor.apply()
            }

            return dailyPick
        } else {
            val pick = applicationSharedPref.getString(Constants.DICTIONARY_PICK_ID, "vocaby")!!
            val random = applicationSharedPref.getBoolean(Constants.DICTIONARY_PICK_RANDOM, true)
            val data: WordDefinitions? = dao.getEntryData(pick)
            return DailyPick(convertToEntryModel(data), random)
        }
    }
}
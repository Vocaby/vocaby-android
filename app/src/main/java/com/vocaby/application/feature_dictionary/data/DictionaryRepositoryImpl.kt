package com.vocaby.application.feature_dictionary.data

import androidx.datastore.core.DataStore
import com.vocaby.app.DictionaryCache
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.data.local.DictionaryDao
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_dictionary.data.remote.DictionaryApi
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.util.EntryConverter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*

class DictionaryRepositoryImpl(
    private val dao: DictionaryDao,
    private val api: DictionaryApi,
    private val dataManager: DataManager,
    private val dictionaryCache: DataStore<DictionaryCache>,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ): DictionaryRepository {
    override suspend fun getEntryDataFromDatabase(entry: String): EntryModel? {
        val wordDefinitions = dao.getEntryData(entry)
        wordDefinitions?.let {
            return EntryConverter.convertFromEntity(it)
        }

        return null
    }

    override suspend fun getEntryIdFromDatabase(entry: String): Long? = dao.getEntryId(entry)

    override suspend fun getRandomEntry(): EntryModel {
        return EntryConverter.convertFromEntity(dao.getRandomWord())
    }

    override suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int = withContext(defaultDispatcher){
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
        id
    }

    override suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel? {
        return try {
            val response = api.checkAndGetDefinitions(entry, date)

            dictionaryCache.updateData { cache ->
                cache.toBuilder()
                    .addApiCache(entry)
                    .build()
            }

            response.body()
        } catch (throwable: Throwable) {
            null
        }
    }

    override suspend fun checkApiCache(entry: String): Boolean {
        return dictionaryCache.data.first().apiCacheList.contains(entry)
    }

    override suspend fun clearDictionaryCache() {
        dictionaryCache.updateData { cache ->
            cache.toBuilder()
                .clearApiCache()
                .build()
        }
    }

    override suspend fun getEntriesByCharacterFromDB(character: String): List<String>
        = dao.getDictionaryEntriesByCharacter(character)

    override suspend fun getDailyPickFromApi(): EntryModel? {
        return try {
            val response = api.getWoD(Formatter.formatDateToString(Date().time, precise=false))
            if (response.isSuccessful && response.code() == 200) {
                response.body()
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }

    override suspend fun getCachedPick(): Pair<String, Boolean> {
        val cache = dictionaryCache.data.first()
        val pick = cache.dailyPickEntry
        val isRandom = cache.dailyPickRandom
        return Pair(pick, isRandom)
    }

    override suspend fun cacheDailyPick(entry: String, random: Boolean) {
        dictionaryCache.updateData { cache ->
            cache.toBuilder()
                .setDailyPickEntry(entry)
                .setDailyPickRandom(random)
                .build()
        }
    }


    override fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>? = dataManager.writeHistory(entry)

    override fun getHistory(): LinkedList<SimpleEntryModel>? = dataManager.history
    override fun getHistory(position: Int): String? {
        return dataManager.getHistory(position)
    }

    override fun clearHistory(): List<SimpleEntryModel>? = dataManager.clearHistory()
}
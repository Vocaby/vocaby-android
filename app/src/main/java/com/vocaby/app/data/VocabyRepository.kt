package com.vocaby.app.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.vocaby.app.Constants
import com.vocaby.app.api.ApiManager
import com.vocaby.app.data.dao.VocabyDao
import com.vocaby.app.data.entity.*
import com.vocaby.app.exceptions.IllegalFileException
import com.vocaby.app.models.BasicExportModel
import com.vocaby.app.models.EntryImportData
import com.vocaby.app.models.customentry.DefinitionChanges
import com.vocaby.app.models.customentry.GroupChanges
import com.vocaby.app.models.dictionary.*
import com.vocaby.app.models.profile.FaqModel
import com.vocaby.app.utils.Formatter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.util.*

class VocabyRepository(private val vocabyDao: VocabyDao, val application: Application) {
    private val userSharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)
    private val dictionaryCacheSharedPreferences: SharedPreferences =
        application.getSharedPreferences(Constants.DICTIONARY_CACHE_ID, Context.MODE_PRIVATE)
    private var userId: Int = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1)
    private val dataManager: DataManager = DataManager.getInstance(application)
    private val apiService  = ApiManager.apiService

    /** --------------------- ENTRY -------------------- **/
    suspend fun replaceEntry(original: EntryModel, remote: EntryModel): Int {
        vocabyDao.deleteEntry(Word(original.id))
        val id = vocabyDao.insertEntry(Word(remote.entry, remote.pronunciation, remote.lastUpdated)).toInt()
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

        vocabyDao.insertDefinitions(definitions)
        return id
    }

    suspend fun getEntryDataFromApi(entry: String): EntryModel? {
        return try {
            val response = apiService.getDefinitions(entry)
            response.body()
        } catch (throwable: Throwable) {
            null
        }
    }

    suspend fun getUpdatedDateFromApi(entry: String): LocalDate? {
        return try {
            val response = apiService.checkEntryUpdate(entry)
            if (response.isSuccessful && response.code() == 200) {
                val oldSet = dictionaryCacheSharedPreferences.getStringSet(Constants.SEARCH_CACHE, mutableSetOf<String>())!!
                val newSet = oldSet.toMutableSet()
                newSet.add(entry)
                dictionaryCacheSharedPreferences.edit().putStringSet(Constants.SEARCH_CACHE, newSet).apply()
                LocalDate.parse(response.body())
            } else {
                null
            }
        } catch (throwable: Throwable) {
            null
        }
    }

    fun checkApiCache(entry: String): Boolean {
        val set = dictionaryCacheSharedPreferences.getStringSet(Constants.SEARCH_CACHE, mutableSetOf<String>())!!
        return set.contains(entry)
    }

    fun clearDictionaryCache() {
        dictionaryCacheSharedPreferences.edit().clear().apply()
    }

    /** --------------------- USER -------------------- **/
    suspend fun setupUser() {
        val exists = vocabyDao.checkUser(userId)
        if (!exists) {
            val id = vocabyDao.createUser(User())
            userId = id.toInt()
            userSharedPreference.edit().putInt(Constants.CURRENT_USER_ID_KEY, userId).apply()
        }
    }

    /** --------------------- ENTRY -------------------- **/
    suspend fun getEntryDataFromDatabase(entry: String): EntryModel?
        = convertToEntryModel(vocabyDao.getEntryData(entry))

    private fun convertToEntryModel(wordDefinitions: WordDefinitions?): EntryModel? {
        wordDefinitions?.let {
            val pronunciation =
                wordDefinitions.wordData.pronunciation?.let { wordDefinitions.wordData.pronunciation }
                    ?: ""

            val wordData = EntryModel(
                wordDefinitions.wordData.id,
                wordDefinitions.wordData.word,
                pronunciation
            )

            wordData.lastUpdated = wordDefinitions.wordData.lastUpdated

            for (data in wordDefinitions.definitions) {
                wordData.addDefinition(data.pos, data.definition, data.sentence)
            }

            return wordData
        }

        return null
    }

    suspend fun getEntriesByCharacterFromDB(character: String): List<String> =
        vocabyDao.getDictionaryEntriesByCharacter(character)

    suspend fun getDailyPick(): DailyPick {
        val randomWordPicker = PreferenceManager.getDefaultSharedPreferences(application)
        val editor = randomWordPicker.edit()
        val lastTimeStarted = randomWordPicker.getInt(Constants.LAST_APP_STARTED, -1)
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]
        val dailyPick: DailyPick

        if (today != lastTimeStarted) {
            dailyPick = try {
                val response = apiService.getWoD(Formatter.formatDateToString(Date().time))
                if (response.isSuccessful && response.code() == 200) {
                    DailyPick(response.body(), false)
                } else {
                    val data: WordDefinitions = vocabyDao.getRandomWord()
                    DailyPick(convertToEntryModel(data), true)
                }
            } catch (throwable: Throwable) {
                val data: WordDefinitions = vocabyDao.getRandomWord()
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
            val pick = randomWordPicker.getString(Constants.DICTIONARY_PICK_ID, "vocaby")!!
            val random = randomWordPicker.getBoolean(Constants.DICTIONARY_PICK_RANDOM, true)
            val data: WordDefinitions? = vocabyDao.getEntryData(pick)
            return DailyPick(convertToEntryModel(data), random)
        }
    }

    suspend fun getAllEntryData(entry: String): EntryModel? {
        val customEntry = getUserEntryData(entry)
        customEntry?.let {
            return customEntry
        } ?: run {
            return getEntryDataFromDatabase(entry)
        }
    }

    /** --------------------- CUSTOM ENTRY -------------------- **/
    suspend fun getUserEntries() = vocabyDao.getUserEntries(userId)
    suspend fun getUserEntryData(entry: String): EntryModel? =
        convertCustomToEntryModel(vocabyDao.getUserEntryData(userId, entry))

    suspend fun removeCustomEntry(entryId: Int) = vocabyDao.deleteUserEntry(entryId)
    suspend fun removeCustomEntry(entry: String) = vocabyDao.deleteUserEntry(entry)
    suspend fun clearUserEntries() = vocabyDao.clearUserEntries(userId)
    suspend fun getAllUserEntries(): List<EntryModel> {
        val entries = vocabyDao.getAllUserEntryData(userId)
        val entryModels = mutableListOf<EntryModel>()
        entries.forEach { entryWithData ->
            val model = convertCustomToEntryModel(entryWithData)
            model?.let {
                entryModels.add(model)
            }
        }

        return entryModels
    }

    suspend fun insertOrUpdateEntry(
        entry: String,
        pronunciation: String,
        groupChanges: GroupChanges,
        definitionChangesMap: MutableMap<String, DefinitionChanges>,
        saveTime: String
    ): Int {
        val entryId: Int = if (groupChanges.entryId == -1) {
            vocabyDao.insertCustomEntry(
                CustomEntry(
                    userId,
                    entry,
                    pronunciation,
                    saveTime
                )
            ).toInt()
        } else {
            vocabyDao.updateCustomEntry(
                CustomEntry(
                    userId,
                    entry,
                    pronunciation,
                    saveTime,
                    groupChanges.entryId
                )
            )

            groupChanges.entryId
        }

        val deletedGroups = mutableListOf<CustomEntryGroup>()
        for (group in groupChanges.deletedItems) {
            deletedGroups.add(CustomEntryGroup(group.groupId, group.type))
        }

        val updatedGroups = mutableListOf<CustomEntryGroup>()
        for (group in groupChanges.updatedItems) {
            updatedGroups.add(
                CustomEntryGroup(
                    group.groupId,
                    entryId,
                    group.type,
                    group.order
                )
            )
        }

        val addedGroups = mutableListOf<CustomEntryGroup>()
        for (group in groupChanges.addedItems) {
            addedGroups.add(
                CustomEntryGroup(
                    entryId,
                    group.type,
                    group.order
                )
            )
        }

        val deletedDefinitions = mutableListOf<CustomDefinition>()
        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.deletedItems) {
                deletedDefinitions.add(
                    CustomDefinition(
                        definitionModel.id,
                        definitionChanges.groupId,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        val updatedDefinitions = mutableListOf<CustomDefinition>()
        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.updatedItems) {
                updatedDefinitions.add(
                    CustomDefinition(
                        definitionModel.id,
                        definitionChanges.groupId,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        vocabyDao.deleteCustomEntryGroups(deletedGroups)
        vocabyDao.updateCustomEntryGroups(updatedGroups)

        val ids = vocabyDao.insertCustomEntryGroups(addedGroups)
        val newGroups = groupChanges.addedItems
        for (i in newGroups.indices) {
            val definitionChanges =
                definitionChangesMap[newGroups[i].type]
            if (definitionChanges != null) definitionChanges.groupId =
                ids[i].toInt()
        }

        val addedDefinitions = mutableListOf<CustomDefinition>()
        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.addedItems) {
                addedDefinitions.add(
                    CustomDefinition(
                        definitionChanges.groupId,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        vocabyDao.insertCustomDefinitions(addedDefinitions)
        vocabyDao.updateCustomDefinitions(updatedDefinitions)
        vocabyDao.deleteCustomDefinitions(deletedDefinitions)

        return entryId
    }

    suspend fun insertNewEntries(data: EntryImportData) {
        val entryIds = vocabyDao.insertCustomEntries(data.entries)
        for (i in entryIds.indices) {
            val addedGroups = mutableListOf<CustomEntryGroup>()
            for (group in data.entryModels[i].definitionGroups)
                addedGroups.add(
                    CustomEntryGroup(
                        entryIds[i].toInt(),
                        group.type,
                        group.order
                    )
                )

            val groupIds = vocabyDao.insertCustomEntryGroups(addedGroups)
            for (j in groupIds.indices) {
                val addedDefinitions = mutableListOf<CustomDefinition>()
                val definitionModels = data.entryModels[i].definitionGroups[j].definitionData
                for (definitionModel in definitionModels) {
                    addedDefinitions.add(
                        CustomDefinition(
                            groupIds[j].toInt(),
                            definitionModel.definition,
                            definitionModel.example,
                            definitionModel.order
                        )
                    )
                }

                vocabyDao.insertCustomDefinitions(addedDefinitions)
            }
        }
    }

    private fun convertCustomToEntryModel(data: EntryWithData?): EntryModel? {
        data?.let {
            val entryData = EntryModel(
                data.customEntry.entryId,
                data.customEntry.entry,
                data.customEntry.pronunciation,
                data.customEntry.lastUpdated
            )

            val groups: MutableList<DefinitionGroupModel> = ArrayList()
            for (group in data.groups) {
                val groupModel = DefinitionGroupModel(
                    group.entryGroup.groupId,
                    group.entryGroup.type,
                    group.entryGroup.order
                )

                val definitions: MutableList<DefinitionModel> = ArrayList()
                for (definitionData in group.definitions) {
                    val definitionModel = DefinitionModel(
                        definitionData.definitionId,
                        group.entryGroup.type,
                        definitionData.definition,
                        definitionData.example,
                        definitionData.order
                    )
                    definitions.add(definitionModel)
                }

                definitions.sort()
                groupModel.definitionData = definitions
                groups.add(groupModel)
            }

            groups.sort()
            entryData.definitionGroups = groups
            return entryData
        }

        return null
    }

    /** --------------------- SAVES -------------------- **/
    fun getSavedWordsFlow() = vocabyDao.getSavesFlow(userId)
    fun hasSaved(entry: String) = vocabyDao.hasSave(userId, entry)
    suspend fun getSavedWords() = vocabyDao.getSaves(userId)
    suspend fun addSaveItem(entry: String) {
        vocabyDao.addSave(UserSave(userId, entry))
    }

    suspend fun addSaveItems(saves: List<UserSave>) {
        vocabyDao.addSaves(saves)
    }

    suspend fun removeSaveItem(entry: String) = vocabyDao.removeSave(userId, entry)
    suspend fun clearSaves() = vocabyDao.clearSaves(userId)

    /** --------------------- IMPORT / EXPORT -------------------- **/
    private fun checkEntryValidity(entry: String) {
        if (entry.length > Constants.ENTRY_MAX_LENGTH
            || entry.isEmpty()
            || Formatter.containsSpecialCharacter(entry)
        ) {
            throw IllegalFileException(
                IllegalFileException.INVALID_FILE
            )
        }
    }

    fun importSavesFromExternalStorage(uri: Uri): List<UserSave> {
        val inputStream = application.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            val gson = Gson()
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)
            val saves: MutableList<UserSave> = ArrayList()
            if (jsonObject.has(Constants.EXPORT_FILE_TYPE_FIELD)) {
                if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_FILE_TYPE_FIELD)
                        .asString == "saves"
                ) {
                    for (item in jsonObject.getAsJsonArray("data")) {
                        val save = item.asString
                        checkEntryValidity(save)
                        saves.add(UserSave(userId, save))
                    }

                    return saves
                } else {
                    throw IllegalFileException(
                        IllegalFileException.INVALID_FILE
                    )
                }
            } else {
                throw IllegalFileException(
                    IllegalFileException.INVALID_FORMAT
                )
            }
        } catch (error: JsonSyntaxException) {
            throw IllegalFileException(
                IllegalFileException.INVALID_FILE
            )
        } finally {
            inputStream?.close()
            reader.close()
        }
    }

    fun importEntriesFromExternalStorage(uri: Uri): EntryImportData {
        val inputStream = application.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
            val gson = Gson()
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)
            val entries: MutableList<CustomEntry> = ArrayList()
            val entryModels: MutableList<EntryModel> = ArrayList()
            if (jsonObject.has(Constants.EXPORT_FILE_TYPE_FIELD)) {
                if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_FILE_TYPE_FIELD)
                        .asString == "entries"
                ) {
                    for (i in jsonObject.getAsJsonArray("data")) {
                        val item = i.asJsonObject
                        val entry = item.getAsJsonPrimitive("entry").asString
                        val pronunciation = item.getAsJsonPrimitive("pronunciation").asString
                        val lastUpdated = item.getAsJsonPrimitive("lastUpdated").asString

                        // Validations
                        checkEntryValidity(entry)
                        if (!Formatter.dateIsValid(lastUpdated)) {
                            throw IllegalFileException(
                                IllegalFileException.INVALID_FORMAT
                            )
                        }

                        entries.add(
                            CustomEntry(
                                userId,
                                entry,
                                pronunciation,
                                lastUpdated
                            )
                        )

                        val entryModel = EntryModel(userId, entry, null)
                        for (g in item.getAsJsonArray("definitionGroups")) {
                            val group = g.asJsonObject
                            val type = group.getAsJsonPrimitive("type").asString
                            val order = group.getAsJsonPrimitive("order").asInt
                            val groupModel = DefinitionGroupModel(type, order)

                            for (d in group.getAsJsonArray("definitionData")) {
                                val definitionData = d.asJsonObject
                                val definition =
                                    definitionData.getAsJsonPrimitive("definition").asString
                                val example = definitionData.getAsJsonPrimitive("example").asString
                                val definitionOrder =
                                    definitionData.getAsJsonPrimitive("order").asInt

                                if (definition.length >= Constants.DEFINITION_MAX_LENGTH) {
                                    throw IllegalFileException(
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                groupModel.addNewDefinition(
                                    DefinitionModel(
                                        type,
                                        definition,
                                        example,
                                        definitionOrder
                                    )
                                )
                            }

                            entryModel.addDefinitionGroup(groupModel)
                        }

                        entryModels.add(entryModel)
                    }

                    return EntryImportData(entries, entryModels)
                } else {
                    throw IllegalFileException(
                        IllegalFileException.INVALID_FILE
                    )
                }
            } else {
                throw IllegalFileException(
                    IllegalFileException.INVALID_FORMAT
                )
            }
        } catch (error: JsonSyntaxException) {
            throw IllegalFileException(
                IllegalFileException.INVALID_FORMAT
            )
        } finally {
            inputStream?.close()
            reader.close()
        }
    }

    fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(BasicExportModel("entries", entries), bw)
            bw.flush()
            bw.close()
        }
    }

    fun writeSavesJsonToExternalStorage(saves: List<String>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(BasicExportModel("saves", saves), bw)
            bw.flush()
            bw.close()
        }
    }

    fun writeSavesToExternalStorage(saves: List<String>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))

            for (entry in saves) {
                bw.write(entry)
                bw.newLine()
            }

            bw.flush()
            bw.close()
        }
    }

    /** --------------------- HISTORY -------------------- **/
    fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>? =
        dataManager.writeHistory(entry)

    fun getHistory(): LinkedList<SimpleEntryModel>? = dataManager.history
    fun clearHistory(): List<SimpleEntryModel>? = dataManager.clearHistory()

    /** --------------------- TYPES -------------------- **/
    suspend fun getTypes() = vocabyDao.getTypes()

    /** --------------------- DATA -------------------- **/
    suspend fun recordVisit(entryId: Int) {
        vocabyDao.recordVisit(DictionaryViewCount(0, userId, entryId, Formatter.formatDateToString(Date().time)))
    }

    suspend fun recordCustomVisit(entryId: Int) {
        vocabyDao.recordCustomVisit(CustomDictionaryViewCount(0, userId, entryId, Formatter.formatDateToString(Date().time)))
    }

    fun updateChartMode(displayAll: Boolean) {
        userSharedPreference.edit().putBoolean(Constants.CHART_MODE_ID, displayAll).apply()
    }

    fun getChartMode(): Boolean = userSharedPreference.getBoolean(Constants.CHART_MODE_ID, false)

    suspend fun getChartData(size: Int): List<VisitData> {
        val displayAll = userSharedPreference.getBoolean(Constants.CHART_MODE_ID, false)
        return if (displayAll) {
            vocabyDao.getAllSearchData(size)
        } else {
            vocabyDao.getMonthlySearchData(size)
        }
    }

    suspend fun eraseVisitData() {
        vocabyDao.deleteVisit(userId)
        vocabyDao.deleteCustomVisit(userId)
    }

    fun isDataShareEnabled() = userSharedPreference.getBoolean(Constants.DATA_SHARE_ID, true)

    fun setDataShareSettings(enabled: Boolean) {
        userSharedPreference.edit().putBoolean(Constants.DATA_SHARE_ID, enabled).apply()
    }

    /** --------------------- DATA -------------------- **/
    fun getFaq(): List<FaqModel> {
        return listOf(
            FaqModel(
                "Is Vocaby free?",
                "Yup! Vocaby is completely free and has no hidden fees or advertisements."
            ),
            FaqModel(
                "Why are some definitions outdated?",
                "Vocaby is powered by Princeton's Wordnet. " +
                        "At Vocaby, we are maintaining and updating definitions so that " +
                        "you are provided with the most up-to-date definition. " +
                        "If you would like to help improve the dictionary, please submit the form below."
            ),
            FaqModel(
                "Will definitions automatically update on my app?",
                "Yup! Once we make updates to our dictionary, your will retrieve the " +
                        "most up to date definitions on your app. This does require an " +
                        "internet connection though."
            ),
            FaqModel(
                "Does Vocaby collect data from me?",
                "We only collect error related data to improve the app and better your experience with Vocaby. " +
                        "If you don't feel comfortable sharing this data, you can opt out in the Data Management page."
            ),
            FaqModel(
                "If I do share my data, can it be traced back to me?",
                "No, the data does not contain any personally identifiable information that can trace back to you. " +
                        "The data does contain some information about your device but anything shared with us is securely encrypted."
            ),
            FaqModel(
                "Why does my import keep failing?",
                "Please make sure that your exported backup json " +
                        "file was indeed created by the app and was not tampered with. " +
                        "If you continue to experience this issue, please feel " +
                        "free to reach out to us!"
            ),
            FaqModel(
                "Will Vocaby be available on other platforms?",
                "We intend to increase Vocaby's " +
                        "availability across platforms further down the road, but we want " +
                        "to make sure that Vocaby matures on Android first."
            )
        )
    }
}
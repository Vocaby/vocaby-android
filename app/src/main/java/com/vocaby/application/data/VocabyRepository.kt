package com.vocaby.application.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.vocaby.application.Constants
import com.vocaby.application.api.ApiManager
import com.vocaby.application.data.dao.VocabyDao
import com.vocaby.application.data.entity.*
import com.vocaby.application.exceptions.ApiException
import com.vocaby.application.exceptions.IllegalFileException
import com.vocaby.application.models.FeedbackModel
import com.vocaby.application.models.customentry.DefinitionChanges
import com.vocaby.application.models.customentry.GroupChanges
import com.vocaby.application.models.customentry.UserEntry
import com.vocaby.application.models.dictionary.*
import com.vocaby.application.models.profile.FaqModel
import com.vocaby.application.states.ValidState
import com.vocaby.application.utils.Formatter
import com.vocaby.application.utils.Logger
import java.io.BufferedWriter
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.net.UnknownHostException
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

    suspend fun checkAndGetEntryDataFromApi(entry: String, date: String): EntryModel? {
        return try {
            val response = apiService.checkAndGetDefinitions(entry, date)

            val oldSet = dictionaryCacheSharedPreferences.getStringSet(Constants.SEARCH_CACHE, mutableSetOf<String>())!!
            val newSet = oldSet.toMutableSet()
            newSet.add(entry)
            dictionaryCacheSharedPreferences.edit().putStringSet(Constants.SEARCH_CACHE, newSet).apply()

            response.body()
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
                val response = apiService.getWoD(Formatter.formatDateToString(Date().time, precise=false))
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
    suspend fun getUserEntries(): LinkedList<UserEntry> {
        val list = vocabyDao.getUserEntries(userId)
        return LinkedList(list)
    }
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
        saveTime: Date
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

    suspend fun insertNewEntries(data: List<EntryModel>) {
        val customEntries = mutableListOf<CustomEntry>()
        for (entryData in data) {
            customEntries.add(CustomEntry(
                userId,
                entryData.entry,
                entryData.pronunciation,
                entryData.lastUpdated
            ))
        }

        val entryIds = vocabyDao.insertCustomEntries(customEntries)
        val addedGroups = mutableListOf<CustomEntryGroup>()
        for ((i, entryId) in entryIds.withIndex()) {
            for (group in data[i].definitionGroups) {
                addedGroups.add(
                    CustomEntryGroup(
                        entryId.toInt(),
                        group.type,
                        group.order
                    )
                )
            }
        }

        val groupIds = vocabyDao.insertCustomEntryGroups(addedGroups)
        val addedDefinitions = mutableListOf<CustomDefinition>()
        var j = 0
        for (entryModel in data) {
            for (group in entryModel.definitionGroups) {
                val definitionModels = group.definitionData
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
                j++
            }
        }

        vocabyDao.insertCustomDefinitions(addedDefinitions)
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
    fun importSavesFromExternalStorage(uri: Uri): List<UserSave> {
        val inputStream = application.contentResolver.openInputStream(uri)
        val reader = ObjectInputStream(inputStream)
        try {
            val saves = reader.readObject() as List<String>
            val userSaves = mutableListOf<UserSave>()
            for (save in saves) {
                userSaves.add(UserSave(userId, save))
            }

            return userSaves
        } catch (error: Throwable) {
            throw IllegalFileException(
                IllegalFileException.INVALID_FORMAT
            )
        } finally {
            inputStream?.close()
            reader.close()
        }
    }

    fun importEntriesBackupFromExternalStorage(uri: Uri): List<EntryModel> {
        val inputStream = application.contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            ObjectInputStream(ins).use {
                return it.readObject() as List<EntryModel>
            }
        }
    }

    fun writeEntriesBackupToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val os = ObjectOutputStream(outputStream)
            os.writeObject(entries)
            os.flush()
            os.close()
        }
    }

    fun writeSavesBackupToExternalStorage(saves: List<String>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val os = ObjectOutputStream(outputStream)
            os.writeObject(saves)
            os.flush()
            os.close()
        }
    }

    fun writeSavesToExternalStorage(saves: List<String>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(saves, bw)
            bw.flush()
            bw.close()
        }
    }

    fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(entries, bw)
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

    fun isUseConnectionEnabled() = userSharedPreference.getBoolean(Constants.CONNECTION_ID, true)

    fun setConnectionSettings(enabled: Boolean) {
        userSharedPreference.edit().putBoolean(Constants.CONNECTION_ID, enabled).apply()
    }

    fun isDataShareEnabled() = userSharedPreference.getBoolean(Constants.DATA_SHARE_ID, true)

    fun setDataShareSettings(enabled: Boolean) {
        userSharedPreference.edit().putBoolean(Constants.DATA_SHARE_ID, enabled).apply()
    }

    /** --------------------- FEEDBACK -------------------- **/
    suspend fun submitFeedback(feedbackModel: FeedbackModel): ValidState {
        return try {
            val response = apiService.submitFeedback("application/json", feedbackModel)
            if (response.isSuccessful) {
                ValidState.Valid
            } else {
                Logger.reportErrorToBugsnag(ApiException("${response.code()}: ${response.body().toString()}"))
                ValidState.Error("Vocaby's server is down :(")
            }
        } catch (e: UnknownHostException) {
            ValidState.Error("No internet connection")
        } catch (e: Throwable) {
            if (isDataShareEnabled()) Logger.reportErrorToBugsnag(e)
            ValidState.Error("Something went wrong...")
        }
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
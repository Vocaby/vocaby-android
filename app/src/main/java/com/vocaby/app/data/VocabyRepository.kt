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
import com.vocaby.app.data.dao.VocabyDao
import com.vocaby.app.data.entity.*
import com.vocaby.app.models.BasicExportModel
import com.vocaby.app.models.customentry.DefinitionChanges
import com.vocaby.app.models.customentry.GroupChanges
import com.vocaby.app.models.datapackage.EntryDataPackage
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import com.vocaby.app.models.dictionary.DefinitionModel
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.utils.Logger
import com.vocaby.app.utils.StringFormatter
import com.vocaby.app.utils.exception.IllegalFileException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class VocabyRepository(private val vocabyDao: VocabyDao, val application: Application) {
    private val userSharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)
    private val entrySharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.DICTIONARY_ENTRIES_KEY, Context.MODE_PRIVATE)
    private var userId: Int = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1)
    private val dataManager: DataManager = DataManager.getInstance(application)

    /** --------------------- USER -------------------- **/
    suspend fun setupUser() {
        val exists = vocabyDao.checkUser(userId)
        if (!exists) {
            val id = vocabyDao.createUser(User())
            userSharedPreference.edit().putInt(Constants.CURRENT_USER_ID_KEY, id.toInt()).apply()
        }
    }

    /** --------------------- ENTRY -------------------- **/
    suspend fun setupDictionaryEntries() {
        if (!entrySharedPreference.contains("z")) {
            val list = vocabyDao.getDictionaryEntries()
            var i = 0
            var sb = StringBuilder()
            while (i < list.size - 1) {
                if (list[i][0] == list[i + 1][0]) {
                    sb.append(list[i])
                    sb.append(";")
                } else {
                    sb.append(list[i])
                    val editor = entrySharedPreference.edit()
                    editor.putString(list[i].substring(0, 1), sb.toString())
                    editor.apply()
                    sb = StringBuilder()
                }

                i++
            }

            // Last element insertion
            sb.append(list[i])
            val editor = entrySharedPreference.edit()
            editor.putString(list[i].substring(0, 1), sb.toString())
            editor.apply()
        }
    }

    private fun convertToEntryModel(wordDefinitions: WordDefinitions?): EntryModel? {
        wordDefinitions?.let {
            val pronunciation =
                wordDefinitions.wordData.pronunciation?.let { wordDefinitions.wordData.pronunciation } ?: ""

            val wordData = EntryModel(
                wordDefinitions.wordData.id,
                wordDefinitions.wordData.word,
                pronunciation
            )

            for (data in wordDefinitions.definitions) {
                wordData.addDefinition(data.pos, data.definition, data.sentence)
            }

            return wordData
        }

        return null
    }

    fun getEntriesByCharacter(character: String): List<String> {
        val e: String? = entrySharedPreference.getString(character, "")
        return listOf(*e!!.split(";".toRegex()).toTypedArray())
    }

    suspend fun getEntryPackage(entry: String): EntryDataPackage {
        val entryData = convertToEntryModel(vocabyDao.getEntryData(entry))
        val customEntryData = convertCustomToEntryModel(vocabyDao.getUserEntryData(userId, entry))
        return EntryDataPackage(entryData, customEntryData)
    }

    suspend fun getEntryData(entry: String): EntryModel? = convertToEntryModel(vocabyDao.getEntryData(entry))

    suspend fun getRandomEntry(): EntryModel? {
        val randomWordPicker = PreferenceManager.getDefaultSharedPreferences(application)
        val editor = randomWordPicker.edit()
        val lastTimeStarted = randomWordPicker.getInt("appStarted", -1)
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]
        val randomEntry: EntryModel?

        if (today != lastTimeStarted) {
            val data: WordDefinitions = vocabyDao.getRandomWord()
            randomEntry = convertToEntryModel(data)

            randomEntry?.let {
                editor.putInt("randomWordId", randomEntry.id)
                editor.putInt("appStarted", today)
                editor.apply()
            }
        } else {
            val id = randomWordPicker.getInt("randomWordId", 100000)
            val data: WordDefinitions? = vocabyDao.getEntryDataWithId(id)
           randomEntry = convertToEntryModel(data)
        }

        return randomEntry
    }

    suspend fun getAllEntryData(entry: String): EntryModel? {
        val customEntry = getUserEntryData(entry)
        customEntry?.let {
            return customEntry
        } ?: run {
            return getEntryData(entry)
        }
    }

    /** --------------------- CUSTOM ENTRY -------------------- **/
    private fun convertCustomToEntryModel(data: EntryWithData?): EntryModel? {
        data?.let {
            val pronunciation =
                data.customEntry.pronunciation?.let { data.customEntry.pronunciation } ?: ""

            val entryData = EntryModel(
                data.customEntry.entryId,
                data.customEntry.entry,
                pronunciation
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

    private fun deleteCustomEntryFromDictionary(entry: String) {
        val character = entry.substring(0, 1)
        val entries = entrySharedPreference.getString(character, "")
        val sb = java.lang.StringBuilder()
        if (entries!!.isNotEmpty()) {
            val list = listOf(*entries.split(";").toTypedArray())
            for (i in list.indices) {
                if (list[i] != entry) {
                    sb.append(list[i])
                    sb.append(";")
                }
            }

            entrySharedPreference.edit().putString(character, sb.toString()).apply()
        }
    }

    private fun addEntryToDictionary(entry: String) {
        val character = entry.substring(0, 1)
        val entries = entrySharedPreference.getString(character, "")
        val sb = java.lang.StringBuilder()
        var added = false
        if (entries!!.isNotEmpty()) {
            val list = listOf(*entries.split(";").toTypedArray())
            var i = 0
            while (i < list.size) {
                if (!added && entry.compareTo(list[i], ignoreCase = true) < 0) {
                    added = true
                    sb.append(entry)
                    sb.append(";")
                }
                sb.append(list[i])
                sb.append(";")
                i++
            }

            if (!added) {
                sb.append(entry)
            }

            entrySharedPreference.edit().putString(character, sb.toString()).apply()
        }
    }

    suspend fun getUserEntries() = vocabyDao.getUserEntries(userId)
    suspend fun getUserEntryData(entry: String) = convertCustomToEntryModel(vocabyDao.getUserEntryData(userId, entry))
    suspend fun removeCustomEntry(entryId: Int, entry: String) {
        vocabyDao.deleteUserEntry(entryId)
        val exists = vocabyDao.checkEntryExistence(entry)
        if (!exists) deleteCustomEntryFromDictionary(entry)
    }
    suspend fun removeCustomEntry(entry: String) {
        vocabyDao.deleteUserEntry(entry)
        deleteCustomEntryFromDictionary(entry)
    }
    suspend fun clearUserEntries() = vocabyDao.clearUserEntries(userId)

    suspend fun insertOrUpdateEntry(
        entry: String,
        pronunciation: String,
        groupChanges: GroupChanges,
        definitionChangesMap: MutableMap<String, DefinitionChanges>
    ): Int {
        val entryId: Int = if (groupChanges.entryId == -1) {
            val exists = vocabyDao.checkEntryExistence(entry)
            if (!exists) addEntryToDictionary(entry)

            vocabyDao.insertCustomEntry(
                CustomEntry(
                    userId,
                    entry,
                    pronunciation,
                    OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
                )
            ).toInt()
        } else {
            vocabyDao.updateCustomEntry(
                CustomEntry(
                    groupChanges.entryId,
                    userId,
                    entry,
                    pronunciation,
                    OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
                )
            )

            groupChanges.entryId
        }

        val deletedGroups: MutableList<CustomEntryGroup> = ArrayList()
        for (group in groupChanges.deletedItems) {
            deletedGroups.add(CustomEntryGroup(group.groupId, group.type))
        }

        val updatedGroups: MutableList<CustomEntryGroup> =
            ArrayList()
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

        val addedGroups: MutableList<CustomEntryGroup> =
            ArrayList()
        for (group in groupChanges.addedItems) {
            addedGroups.add(
                CustomEntryGroup(
                    entryId,
                    group.type,
                    group.order
                )
            )
        }

        val deletedDefinitions: MutableList<CustomDefinition> =
            ArrayList()
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

        val updatedDefinitions: MutableList<CustomDefinition> =
            ArrayList()
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

        val addedDefinitions: MutableList<CustomDefinition> =
            ArrayList()
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

    /** --------------------- SAVES -------------------- **/
    fun getSavedWordsFlow() = vocabyDao.getSavesFlow(userId)
    fun hasSaved(entry: String) = vocabyDao.hasSave(userId, entry)
    suspend fun getSavedWords() = vocabyDao.getSaves(userId)
    suspend fun addSaveItem(entry: String) {
        Logger.reportToDebug("$userId")
        vocabyDao.addSave(UserSave(userId, entry))
    }
    suspend fun removeSaveItem(entry: String) = vocabyDao.removeSave(userId, entry)
    suspend fun clearSaves() = vocabyDao.clearSaves(userId)

    /** --------------------- IMPORT / EXPORT -------------------- **/
    suspend fun importSavesFromExternalStorage(uri: Uri) = withContext(Dispatchers.IO) {
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
                        saves.add(
                            UserSave(
                                userId,
                                StringFormatter.cleanText(item.asString)
                            )
                        )
                    }

                    vocabyDao.addSaves(saves)
                } else {
                    throw IllegalFileException(IllegalFileException.INVALID_FILE)
                }
            } else {
                throw IllegalFileException(IllegalFileException.INVALID_FORMAT)
            }
        } catch (error: JsonSyntaxException) {
            throw IllegalFileException(IllegalFileException.INVALID_FORMAT)
        } finally {
            inputStream?.close()
            reader.close()
        }
    }

    suspend fun writeSavesJsonToExternalStorage(saves: List<String>, uri: Uri)
    = withContext(Dispatchers.IO) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(BasicExportModel("saves", saves), bw)
            bw.flush()
            bw.close()
        }
    }

    suspend fun writeSavesToExternalStorage(saves: List<String>, uri: Uri)
    = withContext(Dispatchers.IO) {
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))

            for (i in saves.indices) {
                bw.write(saves[i])
                bw.newLine()
            }

            bw.flush()
            bw.close()
        }
    }

    /** --------------------- HISTORY -------------------- **/
    fun writeToHistory(entry: String): List<String>? = dataManager.writeHistory(entry)
    fun getHistory(): LinkedList<String>? = dataManager.history
    fun clearHistory(): List<String>? = dataManager.clearHistory()

    /** --------------------- TYPES -------------------- **/
    suspend fun getTypes() = vocabyDao.getTypes()
}
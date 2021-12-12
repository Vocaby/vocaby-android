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
import com.vocaby.app.exceptions.IllegalFileException
import com.vocaby.app.models.BasicExportModel
import com.vocaby.app.models.EntryImportData
import com.vocaby.app.models.customentry.DefinitionChanges
import com.vocaby.app.models.customentry.GroupChanges
import com.vocaby.app.models.datapackage.EntryDataPackage
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import com.vocaby.app.models.dictionary.DefinitionModel
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.models.dictionary.SimpleEntryModel
import com.vocaby.app.utils.StringFormatter
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

    suspend fun getEntriesByCharacterFromDB(character: String): List<String>
        = vocabyDao.getDictionaryEntriesByCharacter(character)

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

    suspend fun getUserEntries() = vocabyDao.getUserEntries(userId)
    suspend fun getUserEntryData(entry: String) = convertCustomToEntryModel(vocabyDao.getUserEntryData(userId, entry))
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
        definitionChangesMap: MutableMap<String, DefinitionChanges>
    ): Int {
        val entryId: Int = if (groupChanges.entryId == -1) {
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
                        val entryModel = EntryModel(item.getAsJsonPrimitive("entry").asString)
                        entryModel.pronunciation = item.getAsJsonPrimitive("pronunciation").asString
                        entries.add(CustomEntry(
                            userId,
                            entryModel.entry,
                            entryModel.pronunciation,
                            OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
                        ))

                        for (g in item.getAsJsonArray("definitionGroups")) {
                            val group = g.asJsonObject
                            val type = group.getAsJsonPrimitive("type").asString
                            val order = group.getAsJsonPrimitive("order").asInt
                            val groupModel = DefinitionGroupModel(type, order)

                            for (d in group.getAsJsonArray("definitionData")) {
                                val definitionData = d.asJsonObject
                                val definition = definitionData.getAsJsonPrimitive("definition").asString
                                val example = definitionData.getAsJsonPrimitive("example").asString
                                val definitionOrder = definitionData.getAsJsonPrimitive("order").asInt
                                groupModel.addNewDefinition(DefinitionModel(type, definition, example, definitionOrder))
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
    fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>? = dataManager.writeHistory(entry)
    fun getHistory(): LinkedList<SimpleEntryModel>? = dataManager.history
    fun clearHistory(): List<SimpleEntryModel>? = dataManager.clearHistory()

    /** --------------------- TYPES -------------------- **/
    suspend fun getTypes() = vocabyDao.getTypes()
}
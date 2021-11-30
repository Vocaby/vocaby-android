package com.vocaby.app.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.vocaby.app.Constants
import com.vocaby.app.data.dao.VocabyDaoKt
import com.vocaby.app.data.entity.EntryWithData
import com.vocaby.app.data.entity.User
import com.vocaby.app.data.entity.UserSave
import com.vocaby.app.data.entity.WordDefinitions
import com.vocaby.app.models.datapackage.EntryDataPackage
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import com.vocaby.app.models.dictionary.DefinitionModel
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.utils.Logger
import java.util.*

class VocabyRepositoryKt(private val vocabyDao: VocabyDaoKt, val application: Application) {
    private val userSharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)
    private val entrySharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.DICTIONARY_ENTRIES_KEY, Context.MODE_PRIVATE)
    private var userId: Int = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1)
    private val dataManager: DataManager = DataManager.getInstance(application)

    /** USER **/
    suspend fun setupUser() = vocabyDao.checkUser(userId)

    /** ENTRY SEARCH **/
    private fun convertToEntryModel(wordDefinitions: WordDefinitions?): EntryModel? {
        wordDefinitions?.let {
            val pronunciation =
                wordDefinitions.word.pronunciation?.let { wordDefinitions.word.pronunciation } ?: ""

            val wordData = EntryModel(
                wordDefinitions.word.id,
                wordDefinitions.word.word,
                pronunciation
            )

            for (data in wordDefinitions.definitions) {
                wordData.addDefinition(data.pos, data.definition, data.sentence)
            }

            return wordData
        }

        return null
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

    fun writeToHistory(entry: String): List<String>? = dataManager.writeHistory(entry)
    fun getHistory(): LinkedList<String>? = dataManager.history
    fun clearHistory(): List<String>? = dataManager.clearHistory()

    fun getEntriesByCharacter(character: String): List<String> {
        val e: String? = entrySharedPreference.getString(character, "")
        return listOf(*e!!.split(";".toRegex()).toTypedArray())
    }

    suspend fun getEntryPackage(entry: String): EntryDataPackage {
        val entryData = convertToEntryModel(vocabyDao.getEntryData(entry))
        val customEntryData = convertCustomToEntryModel(vocabyDao.getUserEntryData(userId, entry))
        return EntryDataPackage(entryData, customEntryData)
    }

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

    fun deleteEntryFromDictionary(entry: String) {
        val character = entry.substring(0, 1)
        val entries = entrySharedPreference.getString(character, "")
        val sb = java.lang.StringBuilder()
        if (entries!!.isNotEmpty()) {
            val list = Arrays.asList(*entries.split(";").toTypedArray())
            for (i in list.indices) {
                if (list[i] != entry) {
                    sb.append(list[i])
                    sb.append(";")
                }
            }

            entrySharedPreference.edit().putString(character, sb.toString()).apply()
        }
    }

    suspend fun getUserEntries() = vocabyDao.getUserEntries(userId);
    suspend fun removeCustomEntry(entry: String) {
        vocabyDao.deleteUserEntry(entry)
        deleteEntryFromDictionary(entry)
    }
    suspend fun clearUserEntries() = vocabyDao.clearUserEntries(userId)

    /** SAVES **/
    fun getSavedWords() = vocabyDao.getSaves(userId)
    fun hasSaved(entry: String) = vocabyDao.hasSave(userId, entry)
    suspend fun addSaveItem(entry: String) = vocabyDao.addSave(UserSave(userId, entry))
    suspend fun removeSaveItem(entry: String) = vocabyDao.removeSave(userId, entry)
    suspend fun clearSaves() = vocabyDao.clearSaves(userId)

    private suspend fun createUser() {
        val longId = vocabyDao.createUser(User())
        userSharedPreference.edit().putInt(Constants.CURRENT_USER_ID_KEY, longId.toInt()).apply()
    }

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
}
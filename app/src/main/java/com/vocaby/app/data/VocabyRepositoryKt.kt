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
import io.reactivex.rxjava3.functions.Consumer
import java.util.*

class VocabyRepositoryKt(val dao: VocabyDaoKt, val application: Application) {
    private val vocabyDao: VocabyDaoKt = dao
    private val userSharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)
    private var userId: Int = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1)
    private val dataManager: DataManager = DataManager.getInstance(application)

    /** USER **/
    suspend fun setupUser() {
        val result = vocabyDao.checkUser(userId)
        if (result == 0) {
            createUser()
            Logger.reportToDebug("Created User using Kotlin")
        } else {
            Logger.reportToDebug("Retrieved User using Kotlin")
        }
    }

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

    fun writeToHistory(entry: String): List<String> {
        return dataManager.writeHistory(entry)
    }

    fun getHistory(): List<String> {
        return dataManager.history
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
}
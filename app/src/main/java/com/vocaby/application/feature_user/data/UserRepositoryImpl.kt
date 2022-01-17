package com.vocaby.application.feature_user.data

import android.content.ContentResolver
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.Logger
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_user.common.Constants
import com.vocaby.application.feature_user.data.local.UserDao
import com.vocaby.application.feature_user.data.local.entity.*
import com.vocaby.application.feature_user.data.remote.UserApi
import com.vocaby.application.feature_user.domain.model.ExportModel
import com.vocaby.application.feature_user.domain.model.FaqModel
import com.vocaby.application.feature_user.domain.model.FeedbackModel
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.states.ValidState
import kotlinx.coroutines.flow.Flow
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.UnknownHostException
import java.util.*

class UserRepositoryImpl constructor(
    private val dao: UserDao,
    private val userApi: UserApi,
    private val dataManager: DataManager,
    private val userSharedPref: SharedPreferences,
    private val contentResolver: ContentResolver
): UserRepository {
    override suspend fun setupUser(userId: Int): Int{
        val exists = dao.checkUser(userId)
        if (!exists) {
            val id = dao.createUser(User()).toInt()
            userSharedPref.edit().putInt(Constants.CURRENT_USER_ID_KEY, id).apply()

            return id
        }

        return userId
    }

    override suspend fun getUser(): Int = userSharedPref.getInt(Constants.CURRENT_USER_ID_KEY, 1)

    override fun getSavedWordsFlow(userId: Int) = dao.getSavesFlow(userId)

    override fun hasSaved(userId: Int, entry: String): Flow<Int> = dao.hasSave(userId, entry)

    override suspend fun getSavedWords(userId: Int)= dao.getSaves(userId)

    override suspend fun addSaveItem(userId: Int, entry: String) {
        dao.addSave(UserSave(userId, entry))
    }

    override suspend fun addSaveItems(saves: List<UserSave>) {
        dao.addSaves(saves)
    }

    override suspend fun removeSaveItem(userId: Int, entry: String) = dao.removeSave(userId, entry)

    override suspend fun clearSaves(userId: Int) = dao.clearSaves(userId)

    override fun importSavesFromExternalStorage(uri: Uri): List<String> {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            JsonReader(InputStreamReader(ins)).use { jsonReader ->
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
                    val saves: MutableList<String> = ArrayList()
                    if (jsonObject.has(Constants.EXPORT_TYPE_FIELD)) {
                        if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_TYPE_FIELD).asString
                            == Constants.EXPORT_SAVE_TYPE
                        ) {
                            for (item in jsonObject.getAsJsonArray("data")) {
                                val entry = item.asString.lowercase()
                                val entryIsValid = Formatter.validateEntry(entry)
                                if (entryIsValid) {
                                    saves.add(entry)
                                } else {
                                    throw IllegalFileException(
                                        "$entry is not valid",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }
                            }

                            return saves
                        } else {
                             throw IllegalFileException(
                                "Backup file has the wrong entry type",
                                IllegalFileException.INVALID_FILE
                             )
                        }
                    } else {
                        throw IllegalFileException(
                            "File is json but not Vocaby's backup",
                            IllegalFileException.INVALID_FORMAT
                        )
                    }
                } catch (e: JsonSyntaxException) {
                    throw IllegalFileException(
                        e.message,
                        IllegalFileException.INVALID_FORMAT
                    )
                }
            }
        }
    }

    override fun importEntriesBackupFromExternalStorage(uri: Uri): List<EntryModel> {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            JsonReader(InputStreamReader(ins)).use { jsonReader ->
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
                    val entryModels: MutableList<EntryModel> = ArrayList()
                    if (jsonObject.has(Constants.EXPORT_TYPE_FIELD)) {
                        if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_TYPE_FIELD).asString
                            == Constants.EXPORT_ENTRY_TYPE
                        ) {
                            for (i in jsonObject.getAsJsonArray("data")) {
                                val item = i.asJsonObject
                                val entry = item.getAsJsonPrimitive("entry").asString.lowercase()
                                val pronunciation = item.getAsJsonPrimitive("pronunciation").asString
                                val lastUpdated = item.getAsJsonPrimitive("lastUpdated").asString

                                // Validations
                                val entryIsValid = Formatter.validateEntry(entry)
                                if (!entryIsValid) {
                                    throw IllegalFileException(
                                        "$entry is not valid",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                if (!Formatter.dateIsValid(lastUpdated)) {
                                    throw IllegalFileException(
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                val exists = entryModels.any { it.entry == entry }

                                if (!exists) {
                                    val entryModel = EntryModel(entry = entry, pronunciation = pronunciation)
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
                                            val definitionOrder = definitionData.getAsJsonPrimitive("order").asInt

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
                            }

                            return entryModels
                        } else {
                            throw IllegalFileException(
                                "Backup file has the wrong entry type",
                                IllegalFileException.INVALID_FILE
                            )
                        }
                    } else {
                        throw IllegalFileException(
                            "File is json but not Vocaby's backup",
                            IllegalFileException.INVALID_FORMAT
                        )
                    }
                } catch (e: JsonSyntaxException) {
                    throw IllegalFileException(
                        e.message,
                        IllegalFileException.INVALID_FORMAT
                    )
                }
            }
        }
    }

    override fun writeSavesToExternalStorage(saves: List<String>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val exportData = ExportModel(Constants.EXPORT_SAVE_TYPE, saves)
            val gson = Gson()
            gson.toJson(exportData, bw)
            bw.flush()
            bw.close()
        }
    }

    override fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val exportData = ExportModel(Constants.EXPORT_ENTRY_TYPE, entries)
            val gson = Gson()
            gson.toJson(exportData, bw)
            bw.flush()
            bw.close()
        }
    }

    override fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>? = dataManager.writeHistory(entry)

    override fun getHistory(): LinkedList<SimpleEntryModel>? = dataManager.history

    override fun clearHistory(): List<SimpleEntryModel>? = dataManager.clearHistory()

    override suspend fun recordVisit(userId: Int, entryId: Int) {
        dao.recordVisit(DictionaryViewCount(0, userId, entryId, Formatter.formatDateToString(Date().time)))
    }

    override suspend fun recordCustomVisit(userId: Int, entryId: Int) {
        dao.recordCustomVisit(CustomDictionaryViewCount(0, userId, entryId, Formatter.formatDateToString(Date().time)))
    }

    override fun updateChartMode(displayAll: Boolean) {
        userSharedPref.edit().putBoolean(Constants.CHART_MODE_ID, displayAll).apply()
    }

    override fun getChartMode(): Boolean = userSharedPref.getBoolean(Constants.CHART_MODE_ID, false)

    override suspend fun getChartData(size: Int): List<VisitData> {
        val displayAll = userSharedPref.getBoolean(Constants.CHART_MODE_ID, false)
        return if (displayAll) {
            dao.getAllSearchData(size)
        } else {
            dao.getMonthlySearchData(size)
        }
    }

    override suspend fun eraseVisitData(userId: Int) {
        dao.deleteVisit(userId)
        dao.deleteCustomVisit(userId)
    }

    override fun isUseConnectionEnabled() = userSharedPref.getBoolean(Constants.CONNECTION_ID, true)

    override fun setConnectionSettings(enabled: Boolean) {
        userSharedPref.edit().putBoolean(Constants.CONNECTION_ID, enabled).apply()
    }

    override fun isDataShareEnabled() = userSharedPref.getBoolean(Constants.DATA_SHARE_ID, true)

    override fun setDataShareSettings(enabled: Boolean) {
        userSharedPref.edit().putBoolean(Constants.DATA_SHARE_ID, enabled).apply()
    }

    override suspend fun submitFeedback(feedbackModel: FeedbackModel): ValidState {
        return try {
            val response = userApi.submitFeedback("application/json", feedbackModel)
            if (response.isSuccessful) {
                ValidState.Valid
            } else {
                if (response.code() >= 500) {
                    ValidState.Error("Vocaby's server is down :(")
                } else {
                    ValidState.Error("Failed to send feedback...")
                }
            }
        } catch (e: UnknownHostException) {
            ValidState.Error("No internet connection")
        } catch (e: Throwable) {
            if (isDataShareEnabled()) Logger.reportErrorToBugsnag(e)
            ValidState.Error("Something went wrong...")
        }
    }

    override fun getFaq(): List<FaqModel> {
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
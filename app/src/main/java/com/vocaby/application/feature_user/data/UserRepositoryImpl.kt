package com.vocaby.application.feature_user.data

import android.content.ContentResolver
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.Logger
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_user.data.local.UserDao
import com.vocaby.application.feature_user.data.local.entity.*
import com.vocaby.application.feature_user.data.remote.UserApi
import com.vocaby.application.feature_user.domain.model.FaqModel
import com.vocaby.application.feature_user.domain.model.FeedbackModel
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.states.ValidState
import kotlinx.coroutines.flow.Flow
import java.io.BufferedWriter
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
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
        val reader = ObjectInputStream(inputStream)
        try {
            return reader.readObject() as List<String>
        } catch (error: Throwable) {
            throw IllegalFileException(
                IllegalFileException.INVALID_FORMAT
            )
        } finally {
            inputStream?.close()
            reader.close()
        }
    }

    override fun importEntriesBackupFromExternalStorage(uri: Uri): List<EntryModel> {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            ObjectInputStream(ins).use {
                return it.readObject() as List<EntryModel>
            }
        }
    }

    override fun writeEntriesBackupToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val os = ObjectOutputStream(outputStream)
            os.writeObject(entries)
            os.flush()
            os.close()
        }
    }

    override fun writeSavesBackupToExternalStorage(saves: List<String>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val os = ObjectOutputStream(outputStream)
            os.writeObject(saves)
            os.flush()
            os.close()
        }
    }

    override fun writeSavesToExternalStorage(saves: List<String>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(saves, bw)
            bw.flush()
            bw.close()
        }
    }

    override fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val gson = Gson()
            gson.toJson(entries, bw)
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
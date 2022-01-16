package com.vocaby.application.feature_user.domain.repository

import android.net.Uri
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_user.data.local.entity.UserSave
import com.vocaby.application.feature_user.data.local.entity.VisitData
import com.vocaby.application.feature_user.domain.model.FaqModel
import com.vocaby.application.feature_user.domain.model.FeedbackModel
import com.vocaby.application.states.ValidState
import kotlinx.coroutines.flow.Flow
import java.util.*

interface UserRepository {
    /** --------------------- USER -------------------- **/
    suspend fun setupUser(userId: Int): Int
    suspend fun getUser(): Int

    /** --------------------- SAVES -------------------- **/
    fun getSavedWordsFlow(userId: Int): Flow<List<String>>
    fun hasSaved(userId: Int, entry: String): Flow<Int>
    suspend fun getSavedWords(userId: Int): List<String>
    suspend fun addSaveItem(userId: Int, entry: String)
    suspend fun addSaveItems(saves: List<UserSave>)
    suspend fun removeSaveItem(userId: Int, entry: String)
    suspend fun clearSaves(userId: Int)

    /** --------------------- IMPORT / EXPORT -------------------- **/
    fun importSavesFromExternalStorage(uri: Uri): List<String>
    fun importEntriesBackupFromExternalStorage(uri: Uri): List<EntryModel>
    fun writeEntriesBackupToExternalStorage(entries: List<EntryModel>, uri: Uri)
    fun writeSavesBackupToExternalStorage(saves: List<String>, uri: Uri)
    fun writeSavesToExternalStorage(saves: List<String>, uri: Uri)
    fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri)

    /** --------------------- HISTORY -------------------- **/
    fun writeToHistory(entry: SimpleEntryModel): List<SimpleEntryModel>?
    fun getHistory(): LinkedList<SimpleEntryModel>?
    fun clearHistory(): List<SimpleEntryModel>?

    /** --------------------- DATA -------------------- **/
    suspend fun recordVisit(userId: Int, entryId: Int)
    suspend fun recordCustomVisit(userId: Int, entryId: Int)
    fun updateChartMode(displayAll: Boolean)
    fun getChartMode(): Boolean
    suspend fun getChartData(size: Int): List<VisitData>
    suspend fun eraseVisitData(userId: Int)
    fun isUseConnectionEnabled(): Boolean
    fun setConnectionSettings(enabled: Boolean)
    fun isDataShareEnabled(): Boolean
    fun setDataShareSettings(enabled: Boolean)

    /** --------------------- SUPPORT -------------------- **/
    suspend fun submitFeedback(feedbackModel: FeedbackModel): ValidState
    fun getFaq(): List<FaqModel>
}
package com.vocaby.application.feature_user.data

import android.content.SharedPreferences
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_user.common.Constants
import com.vocaby.application.feature_user.data.local.UserDao
import com.vocaby.application.feature_user.data.local.entity.*
import com.vocaby.application.feature_user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.util.*

class UserRepositoryImpl constructor(
    private val dao: UserDao,
    private val userSharedPref: SharedPreferences
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
}
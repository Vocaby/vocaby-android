package com.vocaby.application.feature_profile.data

import android.content.SharedPreferences
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_profile.common.Constants
import com.vocaby.application.feature_profile.data.local.UserDao
import com.vocaby.application.feature_profile.data.local.entity.CustomDictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.DictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.User
import com.vocaby.application.feature_profile.domain.model.VisitData
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.UserSave
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


    override suspend fun getSavedWords(userId: Int): List<String> {
        return arrayListOf()
    }

    override suspend fun addSaveItems(saves: List<UserSave>) {
        // dao.addSaves(saves)
    }


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
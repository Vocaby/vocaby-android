package com.vocaby.application.feature_profile.data

import androidx.datastore.core.DataStore
import com.vocaby.app.UserSettings
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.Logger
import com.vocaby.application.feature_profile.data.local.UserDao
import com.vocaby.application.feature_profile.data.local.entity.CustomDictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.DictionaryViewCount
import com.vocaby.application.feature_profile.data.local.entity.User
import com.vocaby.application.feature_profile.domain.model.NotificationFrequency
import com.vocaby.application.feature_profile.domain.model.ProfileModel
import com.vocaby.application.feature_profile.domain.model.VisitData
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*

class UserRepositoryImpl constructor(
    private val dao: UserDao,
    private val userSettingsDataStore: DataStore<UserSettings>,
): UserRepository {
    override fun getProfileData(userId: Int): Flow<ProfileModel?> = dao.getProfileData(userId)

    override val settingsFlow: Flow<UserSettings> = userSettingsDataStore.data.catch { exception ->
        if (exception is IOException) {
            Logger.reportErrorToBugsnag(exception)
            emit(UserSettings.getDefaultInstance())
        } else {
            throw exception
        }
    }

    override fun getCurrentUser(): Flow<Int> = dao.getCurrentUserFlow().filter { it != null }.map { it!!.toInt() }

    override suspend fun setupBaseUser(): Boolean {
        val exists = dao.checkAnyUserExists()
        if (!exists) {
            dao.createUser(User()).toInt()
            userSettingsDataStore.updateData { preferences ->
                preferences.toBuilder()
                    .setReportErrorEnabled(false)
                    .setUpdateDictionaryEnabled(true)
                    .setChartMode(UserSettings.ChartMode.MONTHLY)
                    .setNotificationCollectionId(-1)
                    .setNotificationFrequency(NotificationFrequency.FIFTEEN_MINUTES.value)
                    .setNotificationEnabled(false)
                    .build()
            }
        }

        return true
    }

    override suspend fun getUser(): Int = dao.getCurrentUser()?.toInt() ?: 1

    override suspend fun createUser(): Int = dao.createUser(User()).toInt()

    override suspend fun deleteUser(userId: Int) = dao.deleteUser(User(userId))
    override suspend fun cleanupUser(userId: Int) = dao.cleanupUser(userId)


    override suspend fun recordVisit(userId: Int, entryId: Int) {
        dao.recordVisit(DictionaryViewCount(0, userId, entryId, Formatter.formatDateToString(Date().time)))
    }

    override suspend fun recordCustomVisit(userId: Int, entryId: Int) {
        dao.recordCustomVisit(CustomDictionaryViewCount(0, userId, entryId, Formatter.formatDateToString(Date().time)))
    }

    override suspend fun updateChartMode(mode: UserSettings.ChartMode) {
        userSettingsDataStore.updateData { preferences ->
            preferences.toBuilder().setChartMode(mode).build()
        }
    }

    override suspend fun getChartData(size: Int, chartMode: UserSettings.ChartMode): List<VisitData> {
        return when(chartMode) {
            UserSettings.ChartMode.ALL -> {
                dao.getAllSearchData(size)
            }
            UserSettings.ChartMode.MONTHLY -> {
                dao.getMonthlySearchData(size)
            }
            UserSettings.ChartMode.UNRECOGNIZED -> {
                dao.getAllSearchData(size)
            }
        }
    }

    override suspend fun eraseVisitData(userId: Int) {
        dao.deleteVisit(userId)
        dao.deleteCustomVisit(userId)
    }

    override suspend fun isDictionaryUpdateEnabled(): Boolean = userSettingsDataStore.data.first().updateDictionaryEnabled

    override suspend fun setNotificationSettings(enabled: Boolean) {
        userSettingsDataStore.updateData { preferences ->
            preferences.toBuilder().setNotificationEnabled(enabled).build()
        }
    }

    override suspend fun setConnectionSettings(enabled: Boolean) {
        userSettingsDataStore.updateData { preferences ->
            preferences.toBuilder().setUpdateDictionaryEnabled(enabled).build()
        }
    }

    override suspend fun setDataShareSettings(enabled: Boolean) {
        userSettingsDataStore.updateData { preferences ->
            preferences.toBuilder().setReportErrorEnabled(enabled).build()
        }
    }

    override suspend fun setNotificationCollection(id: Int) {
        userSettingsDataStore.updateData { preferences ->
            preferences.toBuilder().setNotificationCollectionId(id).build()
        }
    }

    override suspend fun setNotificationFrequency(seconds: Int) {
        userSettingsDataStore.updateData { preferences ->
            preferences.toBuilder().setNotificationFrequency(seconds).build()
        }
    }
}
package com.vocaby.application.feature_profile.data

import com.vocaby.app.UserSettings
import com.vocaby.application.feature_profile.data.local.entity.User
import com.vocaby.application.feature_profile.domain.model.FakeUserSettings
import com.vocaby.application.feature_profile.domain.model.NotificationFrequency
import com.vocaby.application.feature_profile.domain.model.ProfileModel
import com.vocaby.application.feature_profile.domain.model.VisitData
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeUserRepository: UserRepository {
    private val userData = mutableListOf<User>()
    private val userSettings = FakeUserSettings()

    override val settingsFlow: Flow<UserSettings>
        get() = TODO("Not yet implemented")

    override fun getCurrentUser(): Flow<Int> = flow {
        emit(userData.first().userId)
    }

    override fun getProfileData(userId: Int): Flow<ProfileModel?> {
        TODO("Not yet implemented")
    }

    override suspend fun setupBaseUser(): Boolean {
        if (userData.isEmpty()) {
            userData.add(User(userId = 1))
            userSettings.notificationEnabled = false
            userSettings.updateDictionaryEnabled = true
            userSettings.reportErrorEnabled = false
            userSettings.notificationCollectionId = -1
            userSettings.notificationFrequency = NotificationFrequency.FIFTEEN_MINUTES.value
            userSettings.chartMode = UserSettings.ChartMode.MONTHLY
        }

        return true
    }

    override suspend fun getUser(): Int = if (userData.isEmpty()) 1 else userData.first().userId

    override suspend fun createUser(): Int {
        val last = userData.last()
        val newUser = User(last.userId+1)
        userData.add(newUser)
        return newUser.userId
    }

    override suspend fun deleteUser(userId: Int) {
        userData.removeIf { it.userId == userId }
    }

    override suspend fun cleanupUser(userId: Int) {
        userData.removeAll { it.userId != userId }
    }

    override suspend fun recordVisit(userId: Int, entryId: Int) {
    }

    override suspend fun recordCustomVisit(userId: Int, entryId: Int) {
    }

    override suspend fun updateChartMode(mode: UserSettings.ChartMode) {
        TODO("Not yet implemented")
    }

    override fun getChartData(size: Int, chartMode: UserSettings.ChartMode): Flow<List<VisitData>> {
        TODO("Not yet implemented")
    }

    override suspend fun eraseVisitData(userId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun isDictionaryUpdateEnabled(): Boolean = userSettings.updateDictionaryEnabled

    override suspend fun setNotificationSettings(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setConnectionSettings(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setDataShareSettings(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setNotificationCollection(id: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun setNotificationFrequency(seconds: Int) {
        TODO("Not yet implemented")
    }
}
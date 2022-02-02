package com.vocaby.application.feature_profile.domain.repository

import com.vocaby.app.UserSettings
import com.vocaby.application.feature_profile.domain.model.VisitData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val settingsFlow: Flow<UserSettings>

    /** --------------------- USER -------------------- **/
    fun getCurrentUser(): Flow<Int>
    suspend fun replaceOwnership(oldUserId: Int, newUserId: Int)
    suspend fun setupBaseUser(userId: Int): Int
    suspend fun setCurrentUser(userId: Int)
    suspend fun getUser(): Int
    suspend fun createUser(): Int
    suspend fun deleteUser(userId: Int)
    suspend fun cleanupUser(userId: Int)

    /** --------------------- DATA -------------------- **/
    suspend fun recordVisit(userId: Int, entryId: Int)
    suspend fun recordCustomVisit(userId: Int, entryId: Int)
    suspend fun updateChartMode(mode: UserSettings.ChartMode)
    suspend fun getChartData(size: Int, chartMode: UserSettings.ChartMode): List<VisitData>
    suspend fun eraseVisitData(userId: Int)
    suspend fun isDictionaryUpdateEnabled(): Boolean
    suspend fun setNotificationSettings(enabled: Boolean)
    suspend fun setConnectionSettings(enabled: Boolean)
    suspend fun setDataShareSettings(enabled: Boolean)
    suspend fun setNotificationCollection(id: Int)
    suspend fun setNotificationFrequency(seconds: Int)
}
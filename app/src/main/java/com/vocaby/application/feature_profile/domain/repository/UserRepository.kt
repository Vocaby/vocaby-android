package com.vocaby.application.feature_profile.domain.repository

import com.vocaby.application.feature_profile.domain.model.VisitData
import com.vocaby.application.feature_save.data.local.entity.UserSave

interface UserRepository {
    /** --------------------- USER -------------------- **/
    suspend fun setupUser(userId: Int): Int
    suspend fun getUser(): Int

    /** --------------------- SAVES -------------------- **/
    suspend fun getSavedWords(userId: Int): List<String>
    suspend fun addSaveItems(saves: List<UserSave>)

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
}
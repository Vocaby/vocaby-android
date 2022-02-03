package com.vocaby.application.core.domain.repository

interface ApplicationRepository {
    suspend fun getLastTimeStarted(): Int
    suspend fun setLastStarted(dayOfYear: Int)
}
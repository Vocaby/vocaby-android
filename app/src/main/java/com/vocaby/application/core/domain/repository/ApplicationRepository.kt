package com.vocaby.application.core.domain.repository

interface ApplicationRepository {
    fun getLastTimeStarted(): Int
    fun setLastStarted(dayOfYear: Int)
}
package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.app.UserSettings
import com.vocaby.application.feature_profile.domain.repository.UserRepository

class UpdateChartModeUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(fetchAllMode: Boolean) {
        val mode = if (fetchAllMode) {
            UserSettings.ChartMode.ALL
        } else {
            UserSettings.ChartMode.MONTHLY
        }

        userRepository.updateChartMode(mode)
    }
}
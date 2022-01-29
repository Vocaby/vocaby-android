package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.NotificationModel
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNotificationSettingsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<NotificationModel> {
        return userRepository.settingsFlow.map {
            NotificationModel(
                it.notificationEnabled,
                it.notificationFrequency,
                it.notificationCollectionId
            )
        }.distinctUntilChanged()
    }
}
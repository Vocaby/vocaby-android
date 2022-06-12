package com.vocaby.application.feature_profile.domain.use_case

class SettingsUseCases(
    val getUserSettingsUseCase: GetUserSettingsUseCase,
    val updateChartModeUseCase: UpdateChartModeUseCase,
    val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    val getNotificationFrequenciesUseCase: GetNotificationFrequenciesUseCase,
    val getNotificationPrioritiesUseCase: GetNotificationPrioritiesUseCase,
    val getSelectedNotificationCollectionUseCase: GetSelectedNotificationCollectionUseCase,
    val getSelectedNotificationFrequencyUseCase: GetSelectedNotificationFrequencyUseCase,
    val getSelectedNotificationPriorityUseCase: GetSelectedNotificationPriorityUseCase,
    val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    val updateNotificationCollectionUseCase: UpdateNotificationCollectionUseCase,
    val updateNotificationFrequencyUseCase: UpdateNotificationFrequencyUseCase,
    val updateNotificationPriorityUseCase: UpdateNotificationPriorityUseCase,
    val updateConnectionSettingsUseCase: UpdateConnectionSettingsUseCase,
    val updateDataShareSettingsUseCase: UpdateDataShareSettingsUseCase
)
package com.vocaby.application.feature_profile.domain.use_case

class SettingsUseCases(
    val getUserSettingsUseCase: GetUserSettingsUseCase,
    val updateChartModeUseCase: UpdateChartModeUseCase,
    val getDataSettingsUseCase: GetDataSettingsUseCase,
    val getDictionarySettingsUseCase: GetDictionarySettingsUseCase,
    val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    val getNotificationFrequenciesUseCase: GetNotificationFrequenciesUseCase,
    val getSelectedNotificationCollectionUseCase: GetSelectedNotificationCollectionUseCase,
    val getSelectedNotificationFrequencyUseCase: GetSelectedNotificationFrequencyUseCase,
    val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    val updateNotificationCollectionUseCase: UpdateNotificationCollectionUseCase,
    val updateNotificationFrequencyUseCase: UpdateNotificationFrequencyUseCase,
    val updateConnectionSettingsUseCase: UpdateConnectionSettingsUseCase,
    val updateDataShareSettingsUseCase: UpdateDataShareSettingsUseCase
)
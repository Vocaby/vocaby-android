package com.vocaby.application.feature_profile.presentation.setting

sealed class SettingsUiEvent {
    data class ShowNotificationFrequencyDialog(val title: String, val items: Array<String>): SettingsUiEvent()
    data class ShowNotificationCollectionDialog(val title: String, val items: Array<String>): SettingsUiEvent()
    data class UpdateNotificationCollection(val collectionName: String): SettingsUiEvent()
    data class UpdateNotificationFrequency(val frequency: String): SettingsUiEvent()
}

package com.vocaby.application.feature_profile.presentation.setting

sealed class SettingsUiEvent {
    data class UpdateSettings(
        val notificationEnabled: Boolean,
        val notificationCollection: String,
        val notificationFrequency: String,
        val autoUpdateEnabled: Boolean,
        val dataShareEnabled: Boolean
    ): SettingsUiEvent()
    data class UpdateNotification(val enabled: Boolean, val minutes: Int): SettingsUiEvent()
    data class ShowNotificationFrequencyDialog(val title: String, val items: ArrayList<String>): SettingsUiEvent()
    data class ShowNotificationCollectionDialog(val title: String, val items: ArrayList<String>): SettingsUiEvent()
    data class UpdateNotificationCollection(val collectionName: String): SettingsUiEvent()
    data class UpdateNotificationFrequency(val frequency: String): SettingsUiEvent()
}

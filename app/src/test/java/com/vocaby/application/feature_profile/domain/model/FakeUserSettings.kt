package com.vocaby.application.feature_profile.domain.model

import com.vocaby.app.UserSettings

data class FakeUserSettings(
    var notificationEnabled: Boolean = false,
    var notificationCollectionId: Int = 0,
    var notificationFrequency: Int = 0,
    var updateDictionaryEnabled: Boolean = false,
    var reportErrorEnabled: Boolean = false,
    var chartMode: UserSettings.ChartMode = UserSettings.ChartMode.ALL
)

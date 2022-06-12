package com.vocaby.application.feature_profile.presentation.setting

import com.vocaby.application.feature_profile.domain.model.NotificationSettings

sealed class DialogUiEvent {
    object ShowAlert: DialogUiEvent()
    data class CloseDialog(val selected: Int, val type: NotificationSettings): DialogUiEvent()
}

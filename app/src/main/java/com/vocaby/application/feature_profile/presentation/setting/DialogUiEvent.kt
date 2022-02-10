package com.vocaby.application.feature_profile.presentation.setting

sealed class DialogUiEvent {
    object ShowAlert: DialogUiEvent()
    data class CloseDialog(val selected: Boolean, val typesChanged: Boolean = false): DialogUiEvent()
}

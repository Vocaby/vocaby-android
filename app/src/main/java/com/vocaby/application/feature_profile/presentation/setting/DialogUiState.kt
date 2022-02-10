package com.vocaby.application.feature_profile.presentation.setting

sealed class DialogUiState {
    object InProgress: DialogUiState()
    object ShowAlert: DialogUiState()
    data class UpdateUi(
        val header: String,
        val items: List<String>
    ): DialogUiState()
}

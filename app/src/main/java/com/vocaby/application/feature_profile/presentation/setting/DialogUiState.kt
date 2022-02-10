package com.vocaby.application.feature_profile.presentation.setting

import com.vocaby.application.feature_dictionary.data.local.entity.Type


sealed class DialogUiState {
    object InProgress: DialogUiState()
    object ShowAlert: DialogUiState()
    data class UpdateUi(
        val types: List<Type>
    ): DialogUiState()
}

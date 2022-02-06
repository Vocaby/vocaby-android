package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder


sealed class DialogUiState {
    object InProgress: DialogUiState()
    object ShowAlert: DialogUiState()
    data class UpdateUi(
        val types: List<String>
    ): DialogUiState()
}

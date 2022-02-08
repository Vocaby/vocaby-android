package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

sealed class DialogUiEvent {
    object ShowAlert: DialogUiEvent()
    data class CloseDialog(val selected: Boolean, val typesChanged: Boolean = false): DialogUiEvent()
}

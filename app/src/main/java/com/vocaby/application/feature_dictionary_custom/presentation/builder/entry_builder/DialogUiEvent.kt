package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

sealed class DialogUiEvent {
    object ShowAlert: DialogUiEvent()
    object CloseDialog: DialogUiEvent()
}

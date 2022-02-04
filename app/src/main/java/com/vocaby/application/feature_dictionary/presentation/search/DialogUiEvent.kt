package com.vocaby.application.feature_dictionary.presentation.search

sealed class DialogUiEvent {
    data class CloseCollectionDialog(
        val removeSave: Boolean,
        val showSnackBar: Boolean = true
    ): DialogUiEvent()
}

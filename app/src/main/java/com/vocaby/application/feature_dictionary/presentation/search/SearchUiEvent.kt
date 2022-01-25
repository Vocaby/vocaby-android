package com.vocaby.application.feature_dictionary.presentation.search

sealed class SearchUiEvent {
    object NoEvent: SearchUiEvent()
    object ShowCollectionDialog: SearchUiEvent()
    data class ShowSnackBar(val message: String, val showAction: Boolean = false): SearchUiEvent()
}

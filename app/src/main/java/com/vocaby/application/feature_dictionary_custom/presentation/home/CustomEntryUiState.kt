package com.vocaby.application.feature_dictionary_custom.presentation.home


sealed class CustomEntryUiState {
    object InProgress: CustomEntryUiState()
    data class UpdateCount(val count: String): CustomEntryUiState()
}

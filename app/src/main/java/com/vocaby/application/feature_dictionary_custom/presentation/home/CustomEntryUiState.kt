package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import java.util.*


sealed class CustomEntryUiState {
    object InProgress: CustomEntryUiState()
    data class ShowEntries(val entries: LinkedList<UserEntry>, val count: String): CustomEntryUiState()
    data class UpdateCount(val count: String): CustomEntryUiState()
}

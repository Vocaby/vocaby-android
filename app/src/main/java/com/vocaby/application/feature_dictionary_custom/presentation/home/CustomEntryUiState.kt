package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import java.util.*


sealed class CustomEntryUiState {
    object InProgress: CustomEntryUiState()
    data class UpdateEntries(
        val entries: LinkedList<UserEntry> = LinkedList(),
        val countText: String = "0 Entry"
    ): CustomEntryUiState()
    data class UpdateCount(val count: String): CustomEntryUiState()
}

package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.core.states.ItemState
import java.util.*


sealed class CustomEntryUiEvent {
    object ResetFilter: CustomEntryUiEvent()
    data class ShowAlert(val message: String): CustomEntryUiEvent()
    data class OpenEntryBuilder(val entry: String, val position: Int = -1): CustomEntryUiEvent()
    data class UpdateAdapter(val position: Int, val state: ItemState): CustomEntryUiEvent()
    data class UpdateEntries(
        val entries: LinkedList<UserEntry> = LinkedList(),
    ): CustomEntryUiEvent()
}

package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.core.states.ItemState


sealed class CustomEntryUiEvent {
    object ResetFilter: CustomEntryUiEvent()
    data class ShowMoreProgress(val scroll: Boolean = false): CustomEntryUiEvent()
    data class AddMoreEntries(val low: Int, val high: Int): CustomEntryUiEvent()
    data class ShowAlert(val message: String): CustomEntryUiEvent()
    data class OpenEntryBuilder(val entry: String, val position: Int = -1): CustomEntryUiEvent()
    data class UpdateAdapter(val position: Int, val state: ItemState): CustomEntryUiEvent()
}

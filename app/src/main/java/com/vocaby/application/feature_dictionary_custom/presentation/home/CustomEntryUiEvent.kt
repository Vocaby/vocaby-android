package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.states.ItemState
import java.util.*


sealed class CustomEntryUiEvent {
    data class ShowAlert(val message: String): CustomEntryUiEvent()
    data class StartEntryBuilder(val entry: String, val position: Int = -1): CustomEntryUiEvent()
    data class UpdateAdapter(val position: Int, val state: ItemState): CustomEntryUiEvent()
}

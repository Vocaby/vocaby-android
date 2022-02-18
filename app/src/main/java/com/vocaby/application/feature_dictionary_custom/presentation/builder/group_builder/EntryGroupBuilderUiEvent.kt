package com.vocaby.application.feature_dictionary_custom.presentation.builder.group_builder

import android.content.Intent
import com.vocaby.application.core.states.ItemState


sealed class EntryGroupBuilderUiEvent {
    data class UpdateAdapter(val position: Int, val state: ItemState): EntryGroupBuilderUiEvent()
    data class CloseBuilder(val resultData: Intent): EntryGroupBuilderUiEvent()
    data class ShowAlert(val message: String): EntryGroupBuilderUiEvent()
    object CloseDialog: EntryGroupBuilderUiEvent()
}

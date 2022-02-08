package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import android.content.Intent
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.states.ItemState


sealed class EntryBuilderUiEvent {
    data class ShowTypeSelectionDialog(
        val types: ArrayList<Type>
    ): EntryBuilderUiEvent()
    data class UpdateAdapter(val position: Int, val state: ItemState): EntryBuilderUiEvent()
    data class CloseBuilder(val resultData: Intent): EntryBuilderUiEvent()
    data class OpenGroupBuilder(val selectedType: String): EntryBuilderUiEvent()
    object CancelBuilder: EntryBuilderUiEvent()
}

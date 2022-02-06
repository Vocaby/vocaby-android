package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel

sealed class EntryBuilderUiState {
    object InProgress: EntryBuilderUiState()
    data class UpdateUiState(
        val header: String,
        val entry: String,
        val pronunciation: String,
        val groups: List<DefinitionGroupModel>
    ): EntryBuilderUiState()
}
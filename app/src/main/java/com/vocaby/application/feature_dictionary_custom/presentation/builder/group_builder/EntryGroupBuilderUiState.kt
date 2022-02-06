package com.vocaby.application.feature_dictionary_custom.presentation.builder.group_builder

import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel

sealed class EntryGroupBuilderUiState {
    object InProgress: EntryGroupBuilderUiState()
    data class UpdateUiState(
        val type: String,
        val typeHeader: String,
        val definitions: List<DefinitionModel>
    ): EntryGroupBuilderUiState()
}
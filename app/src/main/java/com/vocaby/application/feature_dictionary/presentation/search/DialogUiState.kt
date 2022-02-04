package com.vocaby.application.feature_dictionary.presentation.search

import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel

sealed class DialogUiState {
    object EmptyState: DialogUiState()
    data class UpdateUi(
        val updateMode: Boolean,
        val collections: List<UpdateSaveCollectionModel>
    ): DialogUiState()
}

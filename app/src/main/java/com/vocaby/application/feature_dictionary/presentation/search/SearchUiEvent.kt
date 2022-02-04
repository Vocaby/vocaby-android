package com.vocaby.application.feature_dictionary.presentation.search

import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel

sealed class SearchUiEvent {
    data class ShowCollectionDialog(
        val entry: String,
        val saveModel: SaveModel?,
        val updateMode: Boolean,
        val collections: ArrayList<UpdateSaveCollectionModel>
    ): SearchUiEvent()
    data class ShowSnackBar(val message: String, val showAction: Boolean = false): SearchUiEvent()
}

package com.vocaby.application.feature_dictionary_custom.presentation.type

import android.content.Intent
import com.vocaby.application.states.ItemState

sealed class TypeUiEvent {
    data class ShowAlert(val message: String): TypeUiEvent()
    data class UpdateAdapter(val position: Int, val state: ItemState): TypeUiEvent()
    object CloseDialog: TypeUiEvent()
    data class CloseEditor(val resultData: Intent): TypeUiEvent()
}

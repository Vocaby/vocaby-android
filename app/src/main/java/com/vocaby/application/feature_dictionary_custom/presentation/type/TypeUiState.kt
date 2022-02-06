package com.vocaby.application.feature_dictionary_custom.presentation.type

import com.vocaby.application.feature_dictionary.data.local.entity.Type


sealed class TypeUiState {
    object InProgress: TypeUiState()
    object ShowAlert: TypeUiState()
    data class UpdateUi(
        val types: List<Type>
    ): TypeUiState()
}

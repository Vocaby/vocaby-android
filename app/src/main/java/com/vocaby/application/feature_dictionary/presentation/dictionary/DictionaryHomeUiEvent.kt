package com.vocaby.application.feature_dictionary.presentation.dictionary

sealed class DictionaryHomeUiEvent {
    data class ShowAlert(val message: String): DictionaryHomeUiEvent()
    data class OpenEntryBuilder(val entry: String): DictionaryHomeUiEvent()
}

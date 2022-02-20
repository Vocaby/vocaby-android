package com.vocaby.application.feature_dictionary.presentation.search

import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult

sealed class SearchState {
    data class Fetched(
        val data: DictionarySearchResult,
        val dictionarySelectorState: DictionarySelectorState,
        val removeSave: Boolean
    ): SearchState()
    data class InProgress(val message: String): SearchState()
}

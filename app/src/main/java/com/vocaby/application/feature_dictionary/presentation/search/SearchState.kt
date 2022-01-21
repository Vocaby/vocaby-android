package com.vocaby.application.feature_dictionary.presentation.search

import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult

data class SearchState(val data: DictionarySearchResult, val dictionarySelectorState: DictionarySelectorState, val removeSave: Boolean)

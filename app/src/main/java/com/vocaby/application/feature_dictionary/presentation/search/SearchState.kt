package com.vocaby.application.feature_dictionary.presentation.search

import com.vocaby.application.feature_dictionary.domain.model.EntryModel

data class SearchState(val data: List<EntryModel?>, val missingDictionary: Int?, val removeSave: Boolean)

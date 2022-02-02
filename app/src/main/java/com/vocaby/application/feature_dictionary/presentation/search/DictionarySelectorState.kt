package com.vocaby.application.feature_dictionary.presentation.search

import com.vocaby.application.R

data class DictionarySelectorState(
    var displayId: Int = R.id.selection_custom,
    var hideId: Int = R.id.selection_original,
    var displayAll: Boolean = false
)
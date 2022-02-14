package com.vocaby.application.feature_dictionary.domain.model

import android.os.Parcelable
import com.vocaby.vocabywidgets.searchview.suggestions.model.SearchSuggestion
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchSuggestionItem(
    private val entry: String
): SearchSuggestion, Parcelable {
    override val body: String get() = entry
}
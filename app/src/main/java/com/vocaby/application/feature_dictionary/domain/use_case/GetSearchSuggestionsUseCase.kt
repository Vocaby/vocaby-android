package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.util.VocabyAlgo
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import com.vocaby.application.feature_dictionary.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetSearchSuggestionsUseCase {
    operator fun invoke(searchQuery: String, entries: List<String>): Flow<List<SearchSuggestionItem>> = flow {
        val searchSuggestionItems = ArrayList<SearchSuggestionItem>()
        if (!entries.isNullOrEmpty()) {
            var index = VocabyAlgo.binarySearchPrefix(entries, searchQuery)
            if (index > -1 && index < entries.size) {
                val it: Iterator<String> = entries.listIterator(index)
                var count = 0
                while (it.hasNext() && count < Constants.DICTIONARY_SUGGESTIONS_THRESHOLD) {
                    val entry = it.next()
                    if (entry.contains(searchQuery)) {
                        searchSuggestionItems.add(SearchSuggestionItem(entry))
                    }

                    count++
                    index++
                }
            }
        }

        emit(searchSuggestionItems)
    }
}
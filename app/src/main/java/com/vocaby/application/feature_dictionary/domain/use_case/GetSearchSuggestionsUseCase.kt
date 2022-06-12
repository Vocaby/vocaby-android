package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.util.VocabyAlgo
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import com.vocaby.application.feature_dictionary.util.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class GetSearchSuggestionsUseCase {
    suspend operator fun invoke(
        searchQuery: String,
        entries: List<String>,
        threshold: Int = Constants.DICTIONARY_SUGGESTIONS_THRESHOLD,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ): List<SearchSuggestionItem> = withContext(defaultDispatcher) {
        val searchSuggestionItems = ArrayList<SearchSuggestionItem>()
        if (!entries.isEmpty()) {
            var index = VocabyAlgo.binarySearchPrefix(entries, searchQuery)
            if (index > -1 && index < entries.size) {
                val it = entries.listIterator(index)
                var count = 0
                while (it.hasNext() && count < threshold) {
                    yield()
                    val entry = it.next()
                    if (entry.contains(searchQuery)) {
                        searchSuggestionItems.add(SearchSuggestionItem(entry))
                    }

                    count++
                    index++
                }
            }
        }

        return@withContext searchSuggestionItems
    }
}
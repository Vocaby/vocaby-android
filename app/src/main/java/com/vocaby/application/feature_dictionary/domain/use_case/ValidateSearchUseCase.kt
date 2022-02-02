package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.util.Formatter

class ValidateSearchUseCase {
    operator fun invoke(entry: String, searchStack: ArrayDeque<String>): String? {
        val searchedEntry = Formatter.cleanText(entry)
        if (searchedEntry.isNotEmpty()) {
            if (searchStack.isNotEmpty()) {
                if (!searchStack.contains(searchedEntry)) {
                    searchStack.removeLast()
                    searchStack.addLast(searchedEntry)
                    return searchedEntry
                }
            } else {
                searchStack.addLast(searchedEntry)
                return searchedEntry
            }
        }

        return null
    }
}
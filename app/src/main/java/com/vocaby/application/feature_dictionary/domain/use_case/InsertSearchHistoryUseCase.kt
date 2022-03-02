package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository

class InsertSearchHistoryUseCase(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(entry: String, dictionaryResult: DictionarySearchResult): List<SimpleEntryModel>? {
        return if (dictionaryResult.size == 0) {
            dictionaryRepository.writeToHistory(SimpleEntryModel(entry, "No definition was found"))
        } else {
            val definition = dictionaryResult.availableEntry?.firstGroup?.let {
                it.definitionData[0].definition
            } ?: "No definition was found"

            dictionaryRepository.writeToHistory(SimpleEntryModel(entry, definition))
        }
    }
}
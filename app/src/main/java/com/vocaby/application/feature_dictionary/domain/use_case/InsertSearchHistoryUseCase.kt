package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository

class InsertSearchHistoryUseCase(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(entry: String, entryList: List<EntryModel?>): List<SimpleEntryModel>? {
        return if (entryList.isEmpty()) {
            dictionaryRepository.writeToHistory(SimpleEntryModel(entry, ""))
        } else {
            val definition = entryList[0]?.firstGroup?.let {
                it.definitionData[0].definition
            } ?: "No definition was found"

            dictionaryRepository.writeToHistory(SimpleEntryModel(entry, definition))
        }
    }
}
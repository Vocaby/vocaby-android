package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import java.util.*

class GetSearchHistoryUseCase(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(): LinkedList<SimpleEntryModel>? {
        return dictionaryRepository.getHistory()
    }
}
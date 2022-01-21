package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository

class GetSearchHistoryItemUseCase(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(position: Int): String? {
        return dictionaryRepository.getHistory(position)
    }
}
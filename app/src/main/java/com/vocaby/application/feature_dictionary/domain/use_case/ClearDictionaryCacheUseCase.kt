package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository

class ClearDictionaryCacheUseCase (
    private val dictionaryRepository: DictionaryRepository
) {
    suspend operator fun invoke() {
        dictionaryRepository.clearDictionaryApiCache()
    }
}
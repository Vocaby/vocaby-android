package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import javax.inject.Inject

class ClearDictionaryCacheUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke() {
        dictionaryRepository.clearDictionaryCache()
    }
}
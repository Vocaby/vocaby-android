package com.vocaby.application.feature_dictionary_custom.domain.use_case

import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository

class RemoveCustomEntryUseCase(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(entry: String) {
        return customDictionaryRepository.removeUserEntry(entry)
    }
}
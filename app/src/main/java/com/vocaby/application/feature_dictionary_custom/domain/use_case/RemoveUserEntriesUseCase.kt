package com.vocaby.application.feature_dictionary_custom.domain.use_case

import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository

class RemoveUserEntriesUseCase(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(userId: Int) {
        return customDictionaryRepository.clearUserEntries(userId)
    }
}
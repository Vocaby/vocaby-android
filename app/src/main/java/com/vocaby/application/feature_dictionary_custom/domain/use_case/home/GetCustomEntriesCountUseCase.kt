package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import kotlinx.coroutines.flow.Flow

class GetCustomEntriesCountUseCase(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    operator fun invoke(userId: Int): Flow<Int> {
        return customDictionaryRepository.getUserEntriesCount(userId)
    }
}
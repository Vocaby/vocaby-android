package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.feature_dictionary_custom.common.Constants
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import java.util.*

class GetCustomEntriesUseCase(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(userId: Int, offset: Int): LinkedList<UserEntry?> {
        return customDictionaryRepository.getUserEntries(userId, Constants.ENTRY_LIMIT, offset)
    }
}
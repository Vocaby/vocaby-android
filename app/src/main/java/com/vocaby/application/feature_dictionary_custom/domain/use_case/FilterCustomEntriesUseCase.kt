package com.vocaby.application.feature_dictionary_custom.domain.use_case

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import java.util.*

class FilterCustomEntriesUseCase(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(userId: Int, prefix: String): LinkedList<UserEntry> {
        return customDictionaryRepository.filterUserEntries(userId, prefix)
    }
}
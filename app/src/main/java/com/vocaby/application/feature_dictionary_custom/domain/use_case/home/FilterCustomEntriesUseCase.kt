package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import java.util.*

class FilterCustomEntriesUseCase(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(prefix: String): LinkedList<UserEntry?> {
        val userId = userRepository.getUser()
        return customDictionaryRepository.filterUserEntries(userId, prefix)
    }
}
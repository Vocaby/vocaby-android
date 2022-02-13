package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository

class RemoveUserEntriesUseCase(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke() {
        val userId = userRepository.getUser()
        return customDictionaryRepository.clearUserEntries(userId)
    }
}
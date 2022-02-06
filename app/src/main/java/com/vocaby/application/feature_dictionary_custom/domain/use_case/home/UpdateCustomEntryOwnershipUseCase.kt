package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import javax.inject.Inject

class UpdateCustomEntryOwnershipUseCase @Inject constructor(
    val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(newUserId: Int) {
        customDictionaryRepository.replaceUser(newUserId)
    }
}
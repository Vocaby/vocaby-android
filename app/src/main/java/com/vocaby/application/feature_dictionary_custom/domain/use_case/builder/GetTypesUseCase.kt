package com.vocaby.application.feature_dictionary_custom.domain.use_case.builder

import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository

class GetTypesUseCase(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(): MutableList<String> {
        return customDictionaryRepository.getTypes() as MutableList<String>
    }
}
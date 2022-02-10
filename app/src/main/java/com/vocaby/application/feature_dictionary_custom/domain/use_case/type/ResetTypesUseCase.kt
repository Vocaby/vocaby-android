package com.vocaby.application.feature_dictionary_custom.domain.use_case.type

import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import javax.inject.Inject

class ResetTypesUseCase @Inject constructor(
    val customDictionaryRepository: CustomDictionaryRepository,
    private val factoryTypes: List<Type>
) {
    suspend operator fun invoke() {
        customDictionaryRepository.clearTypes()
        customDictionaryRepository.insertTypes(factoryTypes)
    }
}
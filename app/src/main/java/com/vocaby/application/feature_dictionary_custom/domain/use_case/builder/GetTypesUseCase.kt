package com.vocaby.application.feature_dictionary_custom.domain.use_case.builder

import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTypesUseCase @Inject constructor(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    operator fun invoke(): Flow<List<Type>> {
        return customDictionaryRepository.getTypes()
    }
}
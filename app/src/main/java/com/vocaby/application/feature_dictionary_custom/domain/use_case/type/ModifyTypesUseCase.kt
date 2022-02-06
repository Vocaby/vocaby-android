package com.vocaby.application.feature_dictionary_custom.domain.use_case.type

import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import javax.inject.Inject

class ModifyTypesUseCase @Inject constructor(
    private val customDictionaryRepository: CustomDictionaryRepository
) {
    suspend operator fun invoke(changes: ItemChangeState<Type>) {
        val addedTypes = changes.addedItems
        val removedTypes = changes.deletedItems
        val updatedTypes = changes.updatedItems
        customDictionaryRepository.insertTypes(addedTypes)
        customDictionaryRepository.removeTypes(removedTypes)
        customDictionaryRepository.updateTypes(updatedTypes)
    }
}
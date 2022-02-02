package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDictionaryEntriesByCharacter(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(query: String): Flow<GenericState<List<String>>> = flow {
        if (query.isEmpty()) {
            emit(GenericState.Success(ArrayList()))
        } else if (query.isNotEmpty()) {
            emit(GenericState.InProgress)
            val initialCharacter = query.substring(0, 1)
            val entries = dictionaryRepository.getEntriesByCharacterFromDB(initialCharacter)
            emit(GenericState.Success(entries))
        }
    }
}
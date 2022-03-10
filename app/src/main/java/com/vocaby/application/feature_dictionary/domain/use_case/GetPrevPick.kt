package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import kotlinx.coroutines.flow.Flow

class GetPrevPick(
    private val dictionaryRepository: DictionaryRepository,
) {
    operator fun invoke(): Flow<Pair<String, String>> {
        return dictionaryRepository.getPrevPick()
    }
}
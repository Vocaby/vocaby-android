package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import java.util.*
import javax.inject.Inject

class GetSearchHistoryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(): LinkedList<SimpleEntryModel>? {
        return dictionaryRepository.getHistory()
    }
}
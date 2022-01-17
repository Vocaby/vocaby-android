package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import javax.inject.Inject

class EraseSearchHistoryUserCase @Inject constructor(
  private val dictionaryRepository: DictionaryRepository
) {
    operator fun invoke(): List<SimpleEntryModel>? {
        return dictionaryRepository.clearHistory()
    }
}
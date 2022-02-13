package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.presentation.search.DictionarySelectorState
import com.vocaby.application.feature_dictionary.presentation.search.SearchState
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import javax.inject.Inject

class GetAllDictionaryEntryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
) {
    suspend operator fun invoke(entry: String): SearchState {
        val userId = userRepository.getUser()
        var originalData = dictionaryRepository.getEntryDataFromDatabase(entry)
        val customData = customDictionaryRepository.getUserEntryData(userId, entry)

        originalData?.let { og ->
            val isCached = dictionaryRepository.checkApiCache(entry)
            val connectionEnabled = userRepository.isDictionaryUpdateEnabled()
            if (!isCached && connectionEnabled) {
                val retrievedEntry = dictionaryRepository.checkAndGetEntryDataFromApi(
                    entry,
                    Formatter.formatDateToString(og.lastUpdated.time,  precise=false)
                )
                retrievedEntry?.let { newEntry ->
                    newEntry.id = dictionaryRepository.replaceEntry(og, retrievedEntry)
                    originalData = newEntry
                }
            }

            userRepository.recordVisit(userId, originalData!!.id)
        }

        val dictionarySearchResult = DictionarySearchResult()
        val dictionarySelectorState = DictionarySelectorState()
        var removeSave = false
        if (customData != null && originalData != null) {
            dictionarySearchResult.customModel = customData
            dictionarySearchResult.originalModel = originalData
            dictionarySelectorState.displayAll = true
        } else if (customData != null) {
            // Only Custom Available
            userRepository.recordCustomVisit(userId, customData.id)
            dictionarySearchResult.customModel = customData
        } else {
            // No definition
            if (originalData == null) {
                removeSave = true
            }

            dictionarySearchResult.originalModel = originalData
            dictionarySelectorState.displayId = R.id.selection_original
            dictionarySelectorState.hideId = R.id.selection_custom
        }

        return SearchState(dictionarySearchResult, dictionarySelectorState, removeSave)
    }
}
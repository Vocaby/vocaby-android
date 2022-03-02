package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.presentation.search.DictionarySelectorState
import com.vocaby.application.feature_dictionary.presentation.search.SearchState
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllDictionaryEntryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
) {
    suspend operator fun invoke(entry: String): Flow<SearchState> = flow {
        val userId = userRepository.getUser()

        var currentEntryModel = dictionaryRepository.getEntryDataFromDatabase(entry)
        val customData = customDictionaryRepository.getUserEntryData(userId, entry)

        val isCached = dictionaryRepository.checkApiCache(entry)
        val connectionEnabled = userRepository.isDictionaryUpdateEnabled()
        if (!isCached && connectionEnabled) {
            emit(SearchState.InProgress("Checking update..."))

            val retrievedEntry = currentEntryModel?.lastUpdated?.let {
                dictionaryRepository.checkAndGetEntryDataFromApi(
                    entry,
                    Formatter.formatDateToString(
                        it.time,
                        precise=false
                    )
                )
            }

            retrievedEntry?.let { newEntry ->
                if (currentEntryModel == null) {
                    emit(SearchState.InProgress("Updating entry..."))
                } else {
                    emit(SearchState.InProgress("Adding entry..."))
                }

                newEntry.id = dictionaryRepository.replaceEntry(currentEntryModel?.id, newEntry)
                currentEntryModel = newEntry
            }
        }

        val dictionarySearchResult = DictionarySearchResult()
        val dictionarySelectorState = DictionarySelectorState()
        var removeSave = false
        if (customData != null && currentEntryModel != null) {
            dictionarySearchResult.customModel = customData
            dictionarySearchResult.originalModel = currentEntryModel
            dictionarySelectorState.displayAll = true
            currentEntryModel?.let { userRepository.recordVisit(userId, it.id) }
        } else if (customData != null) {
            // Only Custom Available
            userRepository.recordCustomVisit(userId, customData.id)
            dictionarySearchResult.customModel = customData
        } else {
            // No definition
            if (currentEntryModel == null) {
                removeSave = true
            } else {
                currentEntryModel?.let { userRepository.recordVisit(userId, it.id) }
            }

            dictionarySearchResult.originalModel = currentEntryModel
            dictionarySelectorState.displayId = R.id.selection_original
            dictionarySelectorState.hideId = R.id.selection_custom
        }

        emit(SearchState.Fetched(dictionarySearchResult, dictionarySelectorState, removeSave))
    }
}
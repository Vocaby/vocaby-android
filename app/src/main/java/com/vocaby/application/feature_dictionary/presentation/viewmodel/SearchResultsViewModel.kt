package com.vocaby.application.feature_dictionary.presentation.viewmodel

import androidx.lifecycle.*
import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_customdictionary.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.presentation.ui.SearchResultsFragment
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.states.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val dictionaryRepository: DictionaryRepository,
): ViewModel() {
    private val entry: String = savedStateHandle.get(SearchResultsFragment.ENTRY)!!

    private var _entryData: MutableLiveData<ArrayList<EntryModel?>> = MutableLiveData()
    private var _saveState: SingleLiveEvent<SaveState> = SingleLiveEvent()
    private var _missingDictionary: MutableLiveData<Int> = MutableLiveData()
    private var saved:Boolean = false

    val entryData: LiveData<ArrayList<EntryModel?>> get() = _entryData
    val saveState: LiveData<SaveState> get() = _saveState
    val missingDictionary: LiveData<Int> get() = _missingDictionary

    init {
        val scope = viewModelScope.launch(Dispatchers.IO) {
            val userId = userRepository.getUser()
            _saveState.postValue(SaveState.InProgress)
            userRepository.hasSaved(userId, entry).collect {
                saved = it != 0
                _saveState.postValue(SaveState.Fetched(saved))
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            val userId = userRepository.getUser()
            var originalData = dictionaryRepository.getEntryDataFromDatabase(entry)
            val customData = customDictionaryRepository.getUserEntryData(userId, entry)

            originalData?.let { og ->
                val isCached = dictionaryRepository.checkApiCache(entry)
                val connectionEnabled = userRepository.isUseConnectionEnabled()
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

            val data = ArrayList<EntryModel?>()
            if (customData != null && originalData != null) {
                data.add(customData)
                data.add(originalData)
            } else if (customData != null) {
                // Only Custom Available
                userRepository.recordCustomVisit(userId, customData.id)
                data.add(customData)
                _missingDictionary.postValue(R.id.selection_original)
            } else {
                // No definition
                if (originalData == null) {
                    scope.cancel()
                    _saveState.postValue(SaveState.Remove)
                }

                data.add(originalData)
                _missingDictionary.postValue(R.id.selection_custom)
            }

            _entryData.postValue(data)
        }
    }

    fun resetSaveState() {
        _saveState.postValue(SaveState.Fetched(saved))
    }
}
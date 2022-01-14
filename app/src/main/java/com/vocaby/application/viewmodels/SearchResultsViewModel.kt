package com.vocaby.application.viewmodels

import androidx.lifecycle.*
import com.vocaby.application.R
import com.vocaby.application.data.VocabyRepository
import com.vocaby.application.models.dictionary.EntryModel
import com.vocaby.application.states.SaveState
import com.vocaby.application.utils.Formatter
import com.vocaby.application.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchResultsViewModel(
    private val entry: String,
    private val repository: VocabyRepository
): ViewModel() {
    private var _entryData: MutableLiveData<ArrayList<EntryModel?>> = MutableLiveData()
    private var _saveState: SingleLiveEvent<SaveState> = SingleLiveEvent()
    private var _missingDictionary: MutableLiveData<Int> = MutableLiveData()
    private var saved:Boolean = false

    val entryData: LiveData<ArrayList<EntryModel?>> get() = _entryData
    val saveState: LiveData<SaveState> get() = _saveState
    val missingDictionary: LiveData<Int> get() = _missingDictionary

    init {
        val scope = viewModelScope.launch(Dispatchers.IO) {
            _saveState.postValue(SaveState.InProgress)
            repository.hasSaved(entry).collect {
                saved = it != 0
                _saveState.postValue(SaveState.Fetched(saved))
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            var originalData = repository.getEntryDataFromDatabase(entry)
            val customData = repository.getUserEntryData(entry)

            originalData?.let { og ->
                val isCached = repository.checkApiCache(entry)
                val connectionEnabled = repository.isUseConnectionEnabled()
                if (!isCached && connectionEnabled) {
                    val retrievedEntry = repository.checkAndGetEntryDataFromApi(
                        entry,
                        Formatter.formatDateToString(og.lastUpdated.time,  precise=false)
                    )
                    retrievedEntry?.let { newEntry ->
                        newEntry.id = repository.replaceEntry(og, retrievedEntry)
                        originalData = newEntry
                    }
                }

                repository.recordVisit(originalData!!.id)
            }

            val data = ArrayList<EntryModel?>()
            if (customData != null && originalData != null) {
                data.add(customData)
                data.add(originalData)
            } else if (customData != null) {
                // Only Custom Available
                repository.recordCustomVisit(customData.id)
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

    class Factory(
        private val entry: String,
        private val repository: VocabyRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchResultsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchResultsViewModel(entry, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
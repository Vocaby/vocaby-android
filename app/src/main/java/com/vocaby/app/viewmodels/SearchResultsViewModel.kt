package com.vocaby.app.viewmodels

import androidx.lifecycle.*
import com.vocaby.app.R
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.states.SaveState
import com.vocaby.app.utils.SingleLiveEvent
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

    val entryData: LiveData<ArrayList<EntryModel?>> get() = _entryData
    val saveState: LiveData<SaveState> get() = _saveState
    val missingDictionary: LiveData<Int> get() = _missingDictionary

    init {
        _saveState.postValue(SaveState.InProgress)

        val scope = viewModelScope.launch(Dispatchers.IO) {
            _saveState.postValue(SaveState.InProgress)

            repository.hasSaved(entry).collect {
                _saveState.postValue(SaveState.Fetched(it != 0))
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            val wordPackage = repository.getEntryPackage(entry)
            val data = ArrayList<EntryModel?>()
            if (wordPackage.customData != null && wordPackage.originalData != null) {
                data.add(wordPackage.customData)
                data.add(wordPackage.originalData)
                repository.recordVisit(wordPackage.originalData.id)
            } else if (wordPackage.customData != null) {
                data.add(wordPackage.customData)
                _missingDictionary.postValue(R.id.selection_original)
                repository.recordCustomVisit(wordPackage.customData.id)
            } else {
                if (wordPackage.originalData != null) {
                    repository.recordVisit(wordPackage.originalData.id)
                } else {
                    scope.cancel()
                    _saveState.postValue(SaveState.Remove)
                }

                data.add(wordPackage.originalData)
                _entryData.postValue(data)
                _missingDictionary.postValue(R.id.selection_custom)
            }
        }
    }
}

class SearchResultsViewModelFactory(
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
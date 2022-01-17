package com.vocaby.application.feature_dictionary.presentation.search

import androidx.lifecycle.*
import com.vocaby.application.core.util.Logger
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.use_case.GetAllDictionaryEntryUseCase
import com.vocaby.application.feature_dictionary.domain.use_case.GetSaveUseCase
import com.vocaby.application.states.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSaveUseCase: GetSaveUseCase,
    getAllDictionaryEntryUseCase: GetAllDictionaryEntryUseCase
): ViewModel() {
    private val entry: String = savedStateHandle.get(SearchResultsFragment.ENTRY)!!

    private var _entryData: MutableLiveData<List<EntryModel?>> = MutableLiveData()
    private var _saveState: SingleLiveEvent<SaveState> = SingleLiveEvent()
    private var _missingDictionary: MutableLiveData<Int> = MutableLiveData()
    private var saved:Boolean = false

    val entryData: LiveData<List<EntryModel?>> get() = _entryData
    val saveState: LiveData<SaveState> get() = _saveState
    val missingDictionary: LiveData<Int> get() = _missingDictionary

    init {
        viewModelScope.launch {
            _saveState.postValue(SaveState.InProgress)
            getSaveUseCase(entry).collect { isSaved ->
                Logger.reportToDebug("$isSaved")
                saved = isSaved
                _saveState.postValue(SaveState.Fetched(saved))
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            val searchState = getAllDictionaryEntryUseCase(entry)
            searchState.missingDictionary?.let { _missingDictionary.postValue(it) }
            _entryData.postValue(searchState.data)
            if (searchState.removeSave) _saveState.postValue(SaveState.Remove)
        }
    }

    fun resetSaveState() {
        _saveState.postValue(SaveState.Fetched(saved))
    }
}
package com.vocaby.application.feature_dictionary.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.use_case.GetAllDictionaryEntryUseCase
import com.vocaby.application.feature_dictionary.domain.use_case.GetSaveUseCase
import com.vocaby.application.states.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private var _entryData = MutableSharedFlow<List<EntryModel?>>()
    private var _saveState = MutableSharedFlow<SaveState>()
    private var _missingDictionary = MutableSharedFlow<Int>()
    private var saved:Boolean = false

    val entryData get() = _entryData.asSharedFlow()
    val saveState get() = _saveState.asSharedFlow()
    val missingDictionary get() = _missingDictionary.asSharedFlow()

    init {
        viewModelScope.launch {
            _saveState.emit(SaveState.InProgress)
            getSaveUseCase(entry).collect { isSaved ->
                saved = isSaved
                _saveState.emit(SaveState.Fetched(saved))
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            val searchState = getAllDictionaryEntryUseCase(entry)
            searchState.missingDictionary?.let { _missingDictionary.emit(it) }
            _entryData.emit(searchState.data)
            if (searchState.removeSave) _saveState.emit(SaveState.Remove)
        }
    }

    fun resetSaveState() {
        viewModelScope.launch {
            _saveState.emit(SaveState.Fetched(saved))
        }
    }
}
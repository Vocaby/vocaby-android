package com.vocaby.application.feature_dictionary.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.use_case.GetAllDictionaryEntryUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.AddSaveItemUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.CheckSaveUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.RemoveSaveItemUseCase
import com.vocaby.application.feature_save.presentation.save.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkSaveUseCase: CheckSaveUseCase,
    private val addSaveItemUseCase: AddSaveItemUseCase,
    private val removeSaveItemUseCase: RemoveSaveItemUseCase,
    private val getAllDictionaryEntryUseCase: GetAllDictionaryEntryUseCase
): ViewModel() {
    private val entry: String = savedStateHandle.get(SearchResultsFragment.ENTRY)!!
    private var _entryData = MutableStateFlow<ResourceState<DictionarySearchResult>>(ResourceState.InProgress)
    private var _saveState = MutableSharedFlow<SaveState>()
    private var _dictionarySelectorState = MutableSharedFlow<DictionarySelectorState>()
    private var saved:Boolean = false

    val entryData get() = _entryData.asSharedFlow()
    val saveState get() = _saveState.asSharedFlow()
    val dictionarySelectorState get() = _dictionarySelectorState.asSharedFlow()

    init {
        viewModelScope.launch {
            _saveState.emit(SaveState.InProgress)
            checkSaveUseCase(entry).collect { isSaved ->
                saved = isSaved
                _saveState.emit(SaveState.Processed(saved))
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            _entryData.value = ResourceState.InProgress

            val searchState = getAllDictionaryEntryUseCase(entry)
            if (searchState.removeSave) _saveState.emit(SaveState.Remove)
            else _saveState.emit(SaveState.Show)
            _dictionarySelectorState.emit(searchState.dictionarySelectorState)
            _entryData.value = ResourceState.Success(searchState.data)
        }
    }

    fun saveEntry() {
        viewModelScope.launch {
            _saveState.emit(SaveState.InProgress)
            addSaveItemUseCase.invoke(entry)
        }
    }

    fun unsaveEntry() {
        viewModelScope.launch {
            _saveState.emit(SaveState.InProgress)
            removeSaveItemUseCase.invoke(entry)
        }
    }

    fun resetSaveState() {
        viewModelScope.launch {
            _saveState.emit(SaveState.Processed(saved))
        }
    }
}
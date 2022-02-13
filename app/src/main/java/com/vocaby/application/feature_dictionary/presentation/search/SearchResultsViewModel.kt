package com.vocaby.application.feature_dictionary.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.use_case.GetAllDictionaryEntryUseCase
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.GetSaveCollectionsForUpdateUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.AddSaveItemUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.CheckSaveUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.RemoveSaveItemUseCase
import com.vocaby.application.feature_save.presentation.save.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkSaveUseCase: CheckSaveUseCase,
    private val addSaveItemUseCase: AddSaveItemUseCase,
    private val removeSaveItemUseCase: RemoveSaveItemUseCase,
    private val getAllDictionaryEntryUseCase: GetAllDictionaryEntryUseCase,
    private val getSaveCollectionsUseCase: GetSaveCollectionsForUpdateUseCase,
): ViewModel() {
    private val entry: String = savedStateHandle.get(SearchResultsFragment.ENTRY)!!
    private var saveCollections: List<UpdateSaveCollectionModel> = ArrayList()
    private var saveModel: SaveModel? = null

    private var _entryData = MutableStateFlow<ResourceState<DictionarySearchResult>>(ResourceState.InProgress)
    private var _saveState = MutableStateFlow<SaveState>(SaveState.InProgress)
    private var _dictionarySelectorState = MutableStateFlow(DictionarySelectorState())
    private var _uiEvent = MutableSharedFlow<SearchUiEvent>()

    val entryData get() = _entryData.asStateFlow()
    val saveState get() = _saveState.asStateFlow()
    val dictionarySelectorState get() = _dictionarySelectorState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            checkSaveUseCase(entry).collect {
                saveModel = it
                _saveState.value = SaveState.Processed(it.saved)
            }
        }

        viewModelScope.launch {
            val searchState = getAllDictionaryEntryUseCase(entry)
            if (searchState.removeSave) {
                _saveState.value = SaveState.Remove
            }

            _dictionarySelectorState.value = searchState.dictionarySelectorState
            _entryData.value = ResourceState.Success(searchState.data)
        }

        updateCollections()
    }

    private fun updateCollections() {
        viewModelScope.launch {
            getSaveCollectionsUseCase(entry).collectLatest { collections ->
                saveCollections = collections
            }
        }
    }

    fun saveEntry() {
        viewModelScope.launch {
            _saveState.emit(SaveState.InProgress)
            addSaveItemUseCase(entry)
            if (saveCollections.isEmpty())_uiEvent.emit(SearchUiEvent.ShowSnackBar("Entry saved"))
            else _uiEvent.emit(SearchUiEvent.ShowSnackBar("Entry saved", true))
        }
    }

    fun saveToCollections() {
        viewModelScope.launch {
            _uiEvent.emit(SearchUiEvent.ShowCollectionDialog(
                entry,
                saveModel,
                false,
                ArrayList(saveCollections.map { it.copy() })
            ))
        }
    }

    fun removeSavedEntry(forceRemove: Boolean = false) {
        viewModelScope.launch {
            val showCollections = !forceRemove && saveCollections.any { it.saved }
            if (showCollections) {
                _uiEvent.emit(SearchUiEvent.ShowCollectionDialog(
                    entry,
                    saveModel,
                    true,
                    ArrayList(saveCollections.map { it.copy() })
                ))
            } else {
                removeSave()
            }
        }
    }

    private suspend fun removeSave() {
        _saveState.emit(SaveState.InProgress)
        removeSaveItemUseCase(entry)
        _uiEvent.emit(SearchUiEvent.ShowSnackBar("Entry removed from saved"))
    }
}
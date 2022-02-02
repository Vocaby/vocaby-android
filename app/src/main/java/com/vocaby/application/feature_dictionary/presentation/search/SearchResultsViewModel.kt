package com.vocaby.application.feature_dictionary.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.use_case.GetAllDictionaryEntryUseCase
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.AddSaveToCollectionsUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.GetSaveCollectionsForUpdateUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.RemoveSaveFromCollectionsUseCase
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
    private val addSaveToCollectionsUseCase: AddSaveToCollectionsUseCase,
    private val removeSaveFromCollectionsUseCase: RemoveSaveFromCollectionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
): ViewModel() {
    private val entry: String = savedStateHandle.get(SearchResultsFragment.ENTRY)!!
    private var _entryData = MutableStateFlow<ResourceState<DictionarySearchResult>>(ResourceState.InProgress)
    private var _saveState = MutableStateFlow<SaveState>(SaveState.InProgress)
    private var _dictionarySelectorState = MutableStateFlow(DictionarySelectorState())
    private var _uiEvent = MutableSharedFlow<SearchUiEvent>()
    private var _saveCollections = MutableStateFlow<List<UpdateSaveCollectionModel>>(ArrayList())
    private var saveModel: SaveModel? = null
    private var oldCollections: List<UpdateSaveCollectionModel>? = ArrayList()

    val entryData get() = _entryData.asStateFlow()
    val saveState get() = _saveState.asStateFlow()
    val dictionarySelectorState get() = _dictionarySelectorState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()
    val saveCollections get() = _saveCollections.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().flatMapLatest { userId ->
                checkSaveUseCase(userId, entry)
            }.collect {
                saveModel = it
                _saveState.value = SaveState.Processed(it.saved)
            }
        }

        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { userId ->
                val searchState = getAllDictionaryEntryUseCase(userId, entry)
                if (searchState.removeSave) {
                    _saveState.value = SaveState.Remove
                }

                _dictionarySelectorState.value = searchState.dictionarySelectorState
                _entryData.value = ResourceState.Success(searchState.data)
            }
        }

        updateCollections()
    }

    private fun updateCollections() {
        viewModelScope.launch {
            getCurrentUserUseCase().flatMapLatest { userId ->
                getSaveCollectionsUseCase(userId, entry)
            }.collectLatest { collections ->
                _saveCollections.value = collections
            }
        }
    }

    fun updateItemInCollections() {
        viewModelScope.launch {
            removeSaveFromCollectionsUseCase(_saveCollections.value)
            addSaveToCollectionsUseCase(saveModel, oldCollections)
            _uiEvent.emit(SearchUiEvent.CloseCollectionDialog)
            _uiEvent.emit(SearchUiEvent.ShowSnackBar("Save collections updated!"))
        }
    }

    fun saveEntry() {
        viewModelScope.launch {
            _saveState.emit(SaveState.InProgress)
            addSaveItemUseCase(entry)
            if (_saveCollections.value.isEmpty())_uiEvent.emit(SearchUiEvent.ShowSnackBar("Entry saved"))
            else _uiEvent.emit(SearchUiEvent.ShowSnackBar("Entry saved", true))
        }
    }

    fun saveToCollections() {
        viewModelScope.launch {
            _uiEvent.emit(SearchUiEvent.ShowAddCollectionDialog)
        }
    }

    fun unsaveEntry(forceRemove: Boolean = false) {
        viewModelScope.launch {
            val showCollections = !forceRemove && _saveCollections.value.any { it.saved }
            if (showCollections) {
                oldCollections = ArrayList(_saveCollections.value)
                _uiEvent.emit(SearchUiEvent.ShowUpdateCollectionDialog)
            } else {
                oldCollections = null
                removeSave()
            }
        }
    }

    private suspend fun removeSave() {
        _saveState.emit(SaveState.InProgress)
        removeSaveItemUseCase(entry)
        _uiEvent.emit(SearchUiEvent.ShowSnackBar("Entry removed from saved"))
    }

    fun addEntryToCollections() {
        viewModelScope.launch {
            addSaveToCollectionsUseCase(saveModel, _saveCollections.value)
            _uiEvent.emit(SearchUiEvent.CloseCollectionDialog)
            _uiEvent.emit(SearchUiEvent.ShowSnackBar("Save collections updated!"))
        }
    }
}
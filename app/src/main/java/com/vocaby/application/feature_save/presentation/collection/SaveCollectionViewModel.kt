package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.GetSavesAsCollectionUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.SaveCollectionUseCases
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SaveCollectionViewModel @Inject constructor(
    private val saveCollectionUseCases: SaveCollectionUseCases,
    private val getSavesAsCollectionUseCase: GetSavesAsCollectionUseCase
): ViewModel() {
    private var collectionId: Int? = null
    private var selectedCollection: String? = null
    private val _allSaveCollectionState = MutableStateFlow(SaveCollectionModel(0, "All Saved Entries", Date(), 0))
    private val _saveCollectionState = MutableStateFlow<List<SaveCollectionModel>>(LinkedList())
    private val _uiEvent = MutableSharedFlow<CollectionItemsUiEvent>()

    val allSaveCollectionState get() = _allSaveCollectionState.asStateFlow()
    val saveCollectionState get() = _saveCollectionState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            getSavesAsCollectionUseCase().collectLatest { allEntriesCollection ->
                _allSaveCollectionState.value = allEntriesCollection
            }
        }

        viewModelScope.launch {
            saveCollectionUseCases.getSaveCollectionsUseCase().collectLatest { collections ->
                _saveCollectionState.value = collections
            }
        }
    }

    fun setCollection(id: Int, collectionName: String) {
        collectionId = id
        selectedCollection = collectionName
    }

    fun updateCollection(newName: String) {
        val id = collectionId
        val name = selectedCollection
        if (id != null && name != null) {
            viewModelScope.launch {
                when (saveCollectionUseCases.updateSaveCollectionUseCase(
                    id,
                    name,
                    newName,
                    _saveCollectionState.value
                )) {
                    is UserInputState.SameInput -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("The collection already exists"))
                    }
                    is UserInputState.EmptyInput -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Please enter a name"))
                    }
                    is UserInputState.Valid -> {
                        _uiEvent.emit(CollectionItemsUiEvent.CloseCollectionDialog)
                    }
                    is UserInputState.NoInput -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Please enter a new name"))
                    }
                    else -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Something went wrong..."))
                    }
                }
            }
        }
    }

    fun removeCollection() {
        collectionId?.let {
            viewModelScope.launch {
                saveCollectionUseCases.removeSaveCollectionUseCase(it)
            }
        }
    }

    fun prepareUpdateDialog() {
        selectedCollection?.let {
            viewModelScope.launch {
                _uiEvent.emit(CollectionItemsUiEvent.ShowUpdateDialog(it))
            }
        }
    }

    fun addSaveCollection(collectionName: String) {
        viewModelScope.launch {
            when (saveCollectionUseCases.addSaveCollectionUseCase(collectionName, _saveCollectionState.value)) {
                is UserInputState.SameInput -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("The collection already exists"))
                }
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Please enter a name"))
                }
                is UserInputState.Valid -> {
                    _uiEvent.emit(CollectionItemsUiEvent.CloseCollectionDialog)
                    _uiEvent.emit(CollectionItemsUiEvent.ScrollToTop)
                }
                else -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Something went wrong..."))
                }
            }
        }
    }
}
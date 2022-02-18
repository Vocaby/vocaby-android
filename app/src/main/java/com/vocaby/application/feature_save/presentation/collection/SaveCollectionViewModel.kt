package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.SaveCollectionUseCases
import com.vocaby.application.core.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SaveCollectionViewModel @Inject constructor(
    private val saveCollectionUseCases: SaveCollectionUseCases,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
): ViewModel() {
    private var selectedCollection: SaveCollectionModel? = null
    private val _saveCollectionState = MutableStateFlow<List<SaveCollectionModel>>(LinkedList())
    private val _uiEvent = MutableSharedFlow<CollectionItemsUiEvent>()

    val saveCollectionState get() = _saveCollectionState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().flatMapLatest { userId ->
                saveCollectionUseCases.getSaveCollectionsUseCase(userId)
            }.collectLatest { collections ->
                _saveCollectionState.value = collections
            }
        }
    }

    fun setCollection(collection: SaveCollectionModel) {
        selectedCollection = collection
        viewModelScope.launch {
            _uiEvent.emit(CollectionItemsUiEvent.ShowActionsDialog)
        }
    }

    fun updateCollection(newName: String) {
        selectedCollection?.let {
            viewModelScope.launch {
                when (saveCollectionUseCases.updateSaveCollectionUseCase(
                    it.id,
                    it.collectionName,
                    newName,
                    _saveCollectionState.value
                )) {
                    is UserInputState.SameInput<*> -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("The collection already exists"))
                    }
                    is UserInputState.LongInput -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("The name is too long"))
                    }
                    is UserInputState.EmptyInput -> {
                        _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Please enter a name"))
                    }
                    is UserInputState.Valid<*> -> {
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

    fun removeCollection(force: Boolean = false) {
        selectedCollection?.let {
            viewModelScope.launch {
                if (force || it.count == 0) {
                    saveCollectionUseCases.removeSaveCollectionUseCase(it.id)
                } else {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowDeletionWarning(it.collectionName))
                }
            }
        }
    }

    fun prepareUpdateDialog() {
        selectedCollection?.let {
            viewModelScope.launch {
                _uiEvent.emit(CollectionItemsUiEvent.ShowUpdateDialog(it.collectionName))
            }
        }
    }

    fun addSaveCollection(collectionName: String) {
        viewModelScope.launch {
            when (saveCollectionUseCases.addSaveCollectionUseCase(collectionName, _saveCollectionState.value)) {
                is UserInputState.SameInput<*> -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("The collection already exists"))
                }
                is UserInputState.LongInput -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("The name is too long"))
                }
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Please enter a name"))
                }
                is UserInputState.NoInput -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("You must enter a name"))
                }
                is UserInputState.Valid<*> -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ScrollToTop)
                    _uiEvent.emit(CollectionItemsUiEvent.CloseCollectionDialog)
                }
                else -> {
                    _uiEvent.emit(CollectionItemsUiEvent.ShowCollectionAlert("Something went wrong..."))
                }
            }
        }
    }
}
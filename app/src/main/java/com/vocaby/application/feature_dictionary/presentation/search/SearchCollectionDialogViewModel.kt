package com.vocaby.application.feature_dictionary.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.AddSaveToCollectionsUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.RemoveSaveFromCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchCollectionDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addSaveToCollectionsUseCase: AddSaveToCollectionsUseCase,
    private val removeSaveFromCollectionsUseCase: RemoveSaveFromCollectionsUseCase,
): ViewModel() {
    private var _uiEvent = MutableSharedFlow<DialogUiEvent>()
    private var _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.EmptyState)
    private var updateMode: Boolean = false
    private var saveModel: SaveModel? = null
    private val collections: ArrayList<UpdateSaveCollectionModel>

    val uiEvent get() = _uiEvent.asSharedFlow()
    val uiState get() = _dialogUiState.asStateFlow()

    init {
        updateMode = savedStateHandle.get(SearchCollectionDialogFragment.UPDATE_MODE)!!
        saveModel = savedStateHandle.get(SearchCollectionDialogFragment.SAVE_MODEL)!!
        collections = savedStateHandle.get(SearchCollectionDialogFragment.SAVE_COLLECTIONS)!!
        _dialogUiState.value = DialogUiState.UpdateUi(
            updateMode,
            collections
        )
    }

    fun updateItemInCollections() {
        var showSnackBar: Boolean
        viewModelScope.launch {
            if (updateMode) {
                val removed = removeSaveFromCollectionsUseCase(collections)
                val added = addSaveToCollectionsUseCase(saveModel, collections)
                showSnackBar = removed || added
            } else {
                showSnackBar = addSaveToCollectionsUseCase(saveModel, collections)
            }

            _uiEvent.emit(DialogUiEvent.CloseCollectionDialog(false, showSnackBar))
        }
    }
}
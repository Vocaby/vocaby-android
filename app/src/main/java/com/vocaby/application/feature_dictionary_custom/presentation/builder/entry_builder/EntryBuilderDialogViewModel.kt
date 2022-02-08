package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import android.app.Activity
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryBuilderDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private var _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.InProgress)
    private var _dialogUiEvent = MutableSharedFlow<DialogUiEvent>()
    private val types: ArrayList<Type> =
        savedStateHandle.get(EntryBuilderGroupDialogFragment.AVAILABLE_TYPES)!!

    val uiState get() = _dialogUiState.asStateFlow()
    val uiEvent get() = _dialogUiEvent.asSharedFlow()

    init {
        _dialogUiState.value = DialogUiState.UpdateUi(types)
    }

    fun handleResult(result: ActivityResult) {
        viewModelScope.launch {
            if (result.resultCode == Activity.RESULT_OK) {
                _dialogUiEvent.emit(DialogUiEvent.CloseDialog(false, typesChanged = true))
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                _dialogUiEvent.emit(DialogUiEvent.CloseDialog(false))
            }
        }
    }

    fun validate(id: Int) {
        viewModelScope.launch {
            if (id != View.NO_ID) {
                _dialogUiEvent.emit(DialogUiEvent.CloseDialog(true))
            } else {
                _dialogUiEvent.emit(DialogUiEvent.ShowAlert)
            }
        }
    }
}
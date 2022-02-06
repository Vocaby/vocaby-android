package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EntryBuilderDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private var _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.InProgress)
    private val types: ArrayList<String> = savedStateHandle.get(EntryBuilderGroupDialogFragment.AVAILABLE_TYPES)!!

    val uiState get() = _dialogUiState.asStateFlow()

    init {
        _dialogUiState.value = DialogUiState.UpdateUi(types)
    }
}
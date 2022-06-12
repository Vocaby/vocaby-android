package com.vocaby.application.feature_profile.presentation.setting

import android.view.View
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.ChipGroup
import com.vocaby.application.feature_profile.domain.model.NotificationSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private var _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.InProgress)
    private var _dialogUiEvent = MutableSharedFlow<DialogUiEvent>()
    private val title: String =
        savedStateHandle.get(NotificationSettingsDialogFragment.TITLE)!!
    private val items: ArrayList<String> =
        savedStateHandle.get(NotificationSettingsDialogFragment.NOTIFICATION_LIST)!!
    private val type: NotificationSettings =
        savedStateHandle.get(NotificationSettingsDialogFragment.TYPE)!!
    private val selectedIndex: Int =
        savedStateHandle.get(NotificationSettingsDialogFragment.SELECTEDINDEX)!!

    val uiState get() = _dialogUiState.asStateFlow()
    val uiEvent get() = _dialogUiEvent.asSharedFlow()

    init {
        _dialogUiState.value = DialogUiState.UpdateUi(
            title,
            items,
            selectedIndex,
        )
    }

    fun validate(id: Int, chipGroup: ChipGroup) {
        viewModelScope.launch {
            if (id != View.NO_ID) {
                val selected = chipGroup.indexOfChild(chipGroup.findViewById(id))
                _dialogUiEvent.emit(DialogUiEvent.CloseDialog(selected, type))
            } else {
                _dialogUiEvent.emit(DialogUiEvent.ShowAlert)
            }
        }
    }
}
package com.vocaby.application.feature_save.presentation.save

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveUseCases: SaveUseCases
): ViewModel() {
    fun clearSaves() = viewModelScope.launch {
        saveUseCases.clearUserSavesUseCase()
    }
}
package com.vocaby.application.feature_save.presentation.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.use_cases.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    private val saveUseCases: SaveUseCases
): ViewModel() {
    private val _savedWords = MutableSharedFlow<List<String>>()
    private val _savesCount =  MutableSharedFlow<Int>()
    val savedWords  get() = _savedWords.asSharedFlow()
    val savesCount get() = _savesCount.asSharedFlow()

    init {
        viewModelScope.launch {
            saveUseCases.getUserSavesUseCase().collect { saves ->
                _savedWords.emit(saves)
                _savesCount.emit(saves.size)
            }
        }
    }

    fun addSaveItem(entry: String) = viewModelScope.launch {
        saveUseCases.addSaveItemUseCase(entry)
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        saveUseCases.removeSaveItemUseCase(entry)
    }

    fun clearSaves() = viewModelScope.launch {
        saveUseCases.clearUserSavesUseCase()
    }

}
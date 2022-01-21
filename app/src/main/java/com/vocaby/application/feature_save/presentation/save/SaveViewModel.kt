package com.vocaby.application.feature_save.presentation.save

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveUseCases: SaveUseCases
): ViewModel() {
    private val showAll: Boolean = savedStateHandle.get(CollectionItemsFragment.COLLECTION_ALL_PARAM)!!
    private val _savedWords = MutableSharedFlow<List<String>>(replay = 1)
    // private val _savesCount =  MutableSharedFlow<Int>()
    val savedWords  get() = _savedWords.asSharedFlow()
    // val savesCount get() = _savesCount.asSharedFlow()

    init {
        viewModelScope.launch {
            if (showAll) {
                saveUseCases.getUserSavesUseCase().collectLatest { saves ->
                    _savedWords.emit(saves)
                    // _savesCount.emit(saves.size)
                }
            }
        }
    }

    fun addSaveItem(entry: String) = viewModelScope.launch {
//        saveUseCases.addSaveItemUseCase(entry)
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        saveUseCases.removeSaveItemUseCase(entry)
    }

    fun clearSaves() = viewModelScope.launch {
        saveUseCases.clearUserSavesUseCase()
    }
}
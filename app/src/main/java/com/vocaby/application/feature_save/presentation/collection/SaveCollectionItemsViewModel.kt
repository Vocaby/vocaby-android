package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SaveCollectionItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveUseCases: SaveUseCases
): ViewModel() {
    private val showAll: Boolean = savedStateHandle.get(SaveCollectionItemsFragment.COLLECTION_ALL_PARAM)!!
    private val name: String = savedStateHandle.get(SaveCollectionItemsFragment.COLLECTION_NAME_PARAM)!!
    private val id: Int = savedStateHandle.get(SaveCollectionItemsFragment.COLLECTION_ID_PARAM)!!
    private val _savedWords = MutableStateFlow<List<String>>(ArrayList())
    private val _savesCount = MutableStateFlow(0)
    val savedWords get() = _savedWords.asStateFlow()
    val saveCount get() = _savesCount.asStateFlow()

    init {
        viewModelScope.launch {
            if (showAll) {
                saveUseCases.getUserSavesUseCase().collectLatest { saves ->
                    _savedWords.emit(saves)
                    _savesCount.emit(saves.size)
                }
            } else {
                saveUseCases.getSaveCollectionItemsUseCase(name).collectLatest { saves ->
                    _savedWords.emit(saves)
                    _savesCount.emit(saves.size)
                }
            }
        }
    }

//    fun addSaveItem(entry: String) = viewModelScope.launch {
//        saveUseCases.addSaveItemUseCase(entry)
//    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        if (showAll) {
            saveUseCases.removeSaveItemUseCase(entry)
        } else {
            saveUseCases.removeCollectionItemUseCase(entry, id)
        }
    }
}
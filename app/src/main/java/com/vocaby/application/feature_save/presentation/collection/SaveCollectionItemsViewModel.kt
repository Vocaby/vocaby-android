package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SaveCollectionItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveUseCases: SaveUseCases,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
): ViewModel() {
    private val name: String = savedStateHandle.get(SaveCollectionItemsFragment.COLLECTION_NAME_PARAM)!!
    private val id: Int = savedStateHandle.get(SaveCollectionItemsFragment.COLLECTION_ID_PARAM)!!
    private val _savedWords = MutableStateFlow<List<String>>(ArrayList())
    private val _savesCount = MutableStateFlow(0)
    val savedWords get() = _savedWords.asStateFlow()
    val saveCount get() = _savesCount.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().flatMapLatest { userId ->
                saveUseCases.getSaveCollectionItemsUseCase(userId, name)
            }.collectLatest { saves ->
                _savedWords.emit(saves)
                _savesCount.emit(saves.size)
            }
        }
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        saveUseCases.removeSaveCollectionItemUseCase(entry, id)
    }
}
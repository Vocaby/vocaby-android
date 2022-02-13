package com.vocaby.application.feature_save.presentation.save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.ClearSaveCollectionsUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SaveViewModel @Inject constructor(
    private val saveUseCases: SaveUseCases,
    private val clearSaveCollectionsUseCase: ClearSaveCollectionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
): ViewModel() {
    private val _savedWords = MutableStateFlow<List<String>>(ArrayList())
    val savedWords get() = _savedWords.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().flatMapLatest { userId ->
                saveUseCases.getUserSavesUseCase(userId)
            }.collect {
                _savedWords.value = it
            }
        }
    }

    fun clearSaves() = viewModelScope.launch {
        saveUseCases.clearUserSavesUseCase()
    }

    fun clearSaveCollections() = viewModelScope.launch {
        clearSaveCollectionsUseCase()
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        saveUseCases.removeSaveItemUseCase(entry)
    }
}
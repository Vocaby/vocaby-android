package com.vocaby.application.feature_save.presentation.save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    private val saveUseCases: SaveUseCases
): ViewModel() {
    private val _savedWords = MutableStateFlow<List<String>>(ArrayList())
    private val _savesCount = MutableStateFlow(0)
    val savedWords get() = _savedWords.asStateFlow()
    val saveCount get() = _savesCount.asStateFlow()

    init {
        viewModelScope.launch {
            saveUseCases.getUserSavesUseCase().collectLatest { saves ->
                _savedWords.emit(saves)
                _savesCount.emit(saves.size)
            }
        }
    }

    fun clearSaves() = viewModelScope.launch {
        saveUseCases.clearUserSavesUseCase()
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        saveUseCases.removeSaveItemUseCase(entry)
    }
}
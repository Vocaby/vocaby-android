package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.use_cases.save.GetUserSavesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SaveCollectionItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserSavesUseCase: GetUserSavesUseCase
): ViewModel() {
    private val showAll: Boolean = savedStateHandle.get(SaveCollectionItemsFragment.COLLECTION_ALL_PARAM)!!
    private val _savedEntries = MutableStateFlow<List<String>>(LinkedList())
    private val _saveCount = MutableStateFlow(0)

    val savedEntries get() = _savedEntries.asStateFlow()
    val saveCount get() = _saveCount.asStateFlow()

    init {
        viewModelScope.launch {
            if (showAll) {
                getUserSavesUseCase().collectLatest { saves ->
                    _savedEntries.emit(saves)
                    _saveCount.emit(saves.size)
                }
            } else {

            }
        }
    }
}
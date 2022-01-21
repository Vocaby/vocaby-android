package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.SaveCollectionUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveCollectionViewModel @Inject constructor(
    private val saveCollectionUseCases: SaveCollectionUseCases
): ViewModel() {
    private val _saveCollectionState = MutableSharedFlow<List<SaveCollectionModel>>()
    val saveCollectionState get() = _saveCollectionState.asSharedFlow()

    init {
        updateSaveCollections()
    }

    fun updateSaveCollections() {
        viewModelScope.launch {
            val list = saveCollectionUseCases.getSaveCollectionsUseCase()
            _saveCollectionState.emit(list)
        }
    }
}
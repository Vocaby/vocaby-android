package com.vocaby.application.feature_save.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.GetSavesAsCollectionUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.SaveCollectionUseCases
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SaveCollectionViewModel @Inject constructor(
    private val saveCollectionUseCases: SaveCollectionUseCases,
    private val getSavesAsCollectionUseCase: GetSavesAsCollectionUseCase
): ViewModel() {
    private val _allSaveCollectionState = MutableStateFlow(SaveCollectionModel(0, "All Saved Entries", Date(), 0))
    private val _saveCollectionState = MutableStateFlow<List<SaveCollectionModel>>(LinkedList())
    private val _createCollectionState = MutableSharedFlow<UserInputState>()

    val allSaveCollectionState get() = _allSaveCollectionState.asStateFlow()
    val saveCollectionState get() = _saveCollectionState.asStateFlow()
    val createCollectionState get() = _createCollectionState.asSharedFlow()

    init {
        viewModelScope.launch {
            getSavesAsCollectionUseCase().collectLatest { allEntriesCollection ->
                _allSaveCollectionState.value = allEntriesCollection
            }
        }

        viewModelScope.launch {
            saveCollectionUseCases.getSaveCollectionsUseCase().collectLatest { collections ->
                _saveCollectionState.value = collections
            }
        }
    }

    fun updateAllSavesCollection() {
//        viewModelScope.launch {
//            val collection = getSavesAsCollectionUseCase()
//            val new = _saveCollectionState.value.toMutableList()
//            new[0] = collection
//            _saveCollectionState.value = new
//        }
    }

    fun addSaveCollection(collectionName: String) {
        viewModelScope.launch {
            saveCollectionUseCases.addSaveCollectionUseCase(collectionName, _saveCollectionState.value).collectLatest {
                _createCollectionState.emit(it)
            }
        }
    }
}
package com.vocaby.application.feature_dictionary_custom.presentation.builder.group_builder

import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.ValidateDefinitionUpdateUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.ValidateDefinitionUseCase
import com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder.EntryBuilderViewModel
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryGroupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val validateDefinitionUseCase: ValidateDefinitionUseCase,
    val validateDefinitionUpdateUseCase: ValidateDefinitionUpdateUseCase
) : ViewModel() {
    private var definitionGroup: DefinitionGroupModel = savedStateHandle.get(EntryBuilderViewModel.GROUP_KEY)!!
    private var definitionChanges: ItemChangeState<DefinitionModel> = savedStateHandle.get(EntryBuilderViewModel.DEFINITION_CHANGES)!!
    private val initialDefinitions: HashMap<String, DefinitionModel> = HashMap()
    private var action: ItemState = savedStateHandle.get(Constants.ITEM_PAYLOAD_KEY)!!

    private val _uiState = MutableStateFlow<EntryGroupBuilderUiState>(EntryGroupBuilderUiState.InProgress)
    private val _uiEvent = MutableSharedFlow<EntryGroupBuilderUiEvent>()

    val uiState get() = _uiState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        val initGroup: DefinitionGroupModel =
            savedStateHandle.get(EntryBuilderViewModel.INITIAL_DEFINITIONS_KEY)!!

        for (def in initGroup.definitionData) {
            initialDefinitions[def.definition] = def
        }

        viewModelScope.launch {
            _uiState.emit(EntryGroupBuilderUiState.UpdateUiState(
                definitionGroup.type,
                Formatter.firstLetterUpperOnly(definitionGroup.type) + " Group",
                definitionGroup.definitionData
            ))
        }
    }

    fun addDefinition(definition: String, example: String) {
        viewModelScope.launch {
            when(validateDefinitionUseCase(definition, definitionGroup)) {
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(EntryGroupBuilderUiEvent.ShowAlert("Please enter a definition"))
                }
                is UserInputState.InvalidInput -> {
                    _uiEvent.emit(EntryGroupBuilderUiEvent.ShowAlert("The definition already exists"))
                }
                else -> {
                    val definitionToAdd = definitionGroup.addNewDefinition(definition, example)
                    definitionChanges.addNew(definition, definitionToAdd)
                    _uiEvent.emit(EntryGroupBuilderUiEvent.UpdateAdapter(0, ItemState.ADD))
                }
            }
        }
    }

    fun updateDefinition(position: Int, oldDefinition: String, oldExample: String, newDefinition: String, newExample: String) {
        viewModelScope.launch {
            when(validateDefinitionUpdateUseCase(position, oldDefinition, newDefinition, oldExample, newExample, definitionGroup)) {
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(EntryGroupBuilderUiEvent.UpdateAdapter(position, ItemState.DELETE))
                }
                is UserInputState.SameInput<*> -> {
                    _uiEvent.emit(EntryGroupBuilderUiEvent.ShowAlert("Enter a new definition"))
                }
                is UserInputState.InvalidInput -> {
                    _uiEvent.emit(EntryGroupBuilderUiEvent.ShowAlert("The definition already exists"))
                }
                else -> {
                    definitionGroup.definitionData.apply {
                        if (oldDefinition != newDefinition || oldExample != newExample) {
                            this[position].definition = newDefinition
                            this[position].example = newExample

                            if (this[position].isNew) definitionChanges.updateNew(
                                oldDefinition,
                                this[position],
                                newDefinition
                            ) else definitionChanges.updateExisting(
                                this[position].id,
                                this[position],
                            )

                            _uiEvent.emit(EntryGroupBuilderUiEvent.UpdateAdapter(position, ItemState.UPDATE))
                        }
                    }
                }
            }
        }
    }

    fun removeDefinition(position: Int) {
        // Remove the definition
        val definitionRemoved = definitionGroup.removeDefinition(position)
        // Add the removed definition to the changes model
        if (definitionRemoved.isNew) definitionChanges.removeNew(definitionRemoved.definition)
        else definitionChanges.removeExisting(definitionRemoved.id, definitionRemoved) // once removed, removed forever
    }

    fun saveEntryGroup() {
        viewModelScope.launch(Dispatchers.Default) {
            val intent = Intent()
            checkForUpdatedItems()
            fixOrdering()

            intent.putExtra(EntryBuilderViewModel.DEFINITION_CHANGES, definitionChanges)
            intent.putExtra(EntryBuilderViewModel.GROUP_KEY, definitionGroup as Parcelable)
            intent.putExtra(Constants.ITEM_PAYLOAD_KEY, action as Parcelable)
            _uiEvent.emit(EntryGroupBuilderUiEvent.CloseBuilder(intent))
        }
    }

    private fun checkForUpdatedItems() {
        if (definitionGroup.definitionData.isNotEmpty() && initialDefinitions.isNotEmpty()) {
            for (i in definitionGroup.definitionData.indices) {
                val currentDefinition = definitionGroup.definitionData[i]
                val originalDefinition = initialDefinitions[currentDefinition.definition]

                if (originalDefinition != null && originalDefinition.id == currentDefinition.id) {
                    if (originalDefinition.order == currentDefinition.order
                        && originalDefinition.example == currentDefinition.example
                    ) {
                        definitionChanges.removeItemUpdated(originalDefinition.id)
                    } else {
                        definitionChanges.putItemUpdated(
                            originalDefinition.id,
                            currentDefinition
                        )
                    }
                }
            }
        }
    }

    // definitionChanges map does not hold references of definitionGroup.definitions when
    // the user updates a group. this is a problem because the setOrder
    // will not be reflected in definition changes. this method will fix that
    private fun fixOrdering() {
        if (action == ItemState.UPDATE) {
            for (def in definitionGroup.definitionData) {
                if (definitionChanges.hasItemAdded(def.definition)) {
                    definitionChanges.putItemAdded(def.definition, def)
                }
            }
        }
    }
}
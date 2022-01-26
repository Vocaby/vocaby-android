package com.vocaby.application.feature_dictionary_custom.presentation.viewmodel

import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.payloads.ItemIntPayload
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState

class EntryGroupViewModel : ViewModel() {
    private lateinit var definitionGroup: DefinitionGroupModel
    private var definitionChanges: ItemChangeState<DefinitionModel> = ItemChangeState()
    private val initialDefinitions: HashMap<String, DefinitionModel> = HashMap()
    private var resultState: ItemState? = null

    private val _type: SingleLiveEvent<String> = SingleLiveEvent()
    private val _definitions: SingleLiveEvent<MutableList<DefinitionModel>> = SingleLiveEvent()
    private val _inputState: SingleLiveEvent<UserInputState> = SingleLiveEvent()
    private val _definitionsState: SingleLiveEvent<ItemIntPayload> = SingleLiveEvent()

    val definitions: LiveData<MutableList<DefinitionModel>> get() = _definitions
    val type: LiveData<String> get() = _type
    val inputState: LiveData<UserInputState> get() = _inputState
    val definitionState: LiveData<ItemIntPayload> get() = _definitionsState

    fun handleIntent(receivedIntent: Intent) {
        definitionGroup = receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY)!!
        _definitions.value = definitionGroup.definitionData
        _type.value = definitionGroup.type

        val initGroup: DefinitionGroupModel =
            receivedIntent.getParcelableExtra(EntryViewModel.INITIAL_DEFINITIONS_KEY)!!

        for (def in initGroup.definitionData) {
            initialDefinitions[def.definition] = def
        }

        definitionChanges = receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES)!!
        resultState = receivedIntent.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)
    }

    fun addDefinition(definition: String, example: String) {
        if (definition.isEmpty()) {
            _inputState.value = UserInputState.EmptyInput
        } else {
            if (definitionGroup.hasDefinition(definition)) {
                _inputState.value = UserInputState.InvalidInput
            } else {
                _inputState.value = UserInputState.Valid("")
                var definitionToAdd = initialDefinitions[definition]

                if (definitionToAdd != null) {
                    definitionToAdd = DefinitionModel(definitionToAdd)
                    definitionToAdd.example = example
                    definitionGroup.addNewDefinition(definitionToAdd)
                } else {
                    definitionToAdd = definitionGroup.addNewDefinition(definition, example)
                }

                definitionChanges.addItem(definition, definitionToAdd)
                _definitionsState.setValue(ItemIntPayload(-1, ItemState.ADD))
            }
        }
    }

    // update, remove, save - right
    // update, save, remove, save - wrong
    // updated item does not reorder
    fun updateDefinition(position: Int, oldDefinition: String, oldExample: String, newDefinition: String, newExample: String) {
        if (newDefinition.isEmpty()) {
            _definitionsState.value = ItemIntPayload(position, ItemState.DELETE)
        } else if (oldDefinition == newDefinition && oldExample == newExample) {
            _inputState.value = UserInputState.SameInput
        } else if (definitionGroup.hasDefinitionExclusive(newDefinition, position)) {
            _inputState.value = UserInputState.InvalidInput
        } else {
            definitionGroup.definitionData.apply {
                if (oldDefinition != newDefinition || oldExample != newExample) {
                    val newModel = DefinitionModel(
                        this[position].id,
                        this[position].type,
                        newDefinition,
                        newExample,
                        this[position].order
                    )

                    this[position] = newModel

                    if (this[position].id == -1) {
                        definitionChanges.removeItemAdded(oldDefinition)
                        definitionChanges.addItem(newDefinition, newModel)
                    } else {
                        definitionChanges.putItemUpdated(newDefinition, newModel)
                    }

                    _definitionsState.value = ItemIntPayload(position, ItemState.UPDATE)
                }
            }
        }
    }

    fun removeDefinition(position: Int) {
        // Remove the definition
        val definitionRemoved = definitionGroup.removeDefinition(position)
        // Add the removed definition to the changes model
        definitionChanges.removeItem(definitionRemoved.definition, definitionRemoved)
    }

    fun addSaveDataToIntent(intent: Intent): Intent {
        checkForUpdatedItems()
        fixOrdering()
        intent.putExtra(EntryViewModel.DEFINITION_CHANGES, definitionChanges)
        intent.putExtra(EntryViewModel.GROUP_KEY, definitionGroup as Parcelable)
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, resultState as Parcelable)
        return intent
    }

    private fun checkForUpdatedItems() {
        if (definitionGroup.definitionData.isNotEmpty() && initialDefinitions.isNotEmpty()) {
            for (i in definitionGroup.definitionData.indices) {
                val currentDefinition = definitionGroup.definitionData[i]
                val originalDefinition = initialDefinitions[currentDefinition.definition]
                if (originalDefinition != null) {
                    if (originalDefinition.order == currentDefinition.order
                        && originalDefinition.example == currentDefinition.example
                    ) {
                        definitionChanges.removeItemUpdated(currentDefinition.definition)
                    } else {
                        definitionChanges.putItemUpdated(
                            currentDefinition.definition,
                            currentDefinition
                        )
                    }
                }
            }
        }
    }

    // definitionChanges map does not hold references of definitionGroup.definitions when
    // the user updates a newly created group. this is a problem because the setOrder
    // will not be reflected in definition changes. this method will fix that
    private fun fixOrdering() {
        if (resultState == ItemState.UPDATE) {
            for (def in definitionGroup.definitionData) {
                if (definitionChanges.hasItemAdded(def.definition)) {
                    definitionChanges.putItemAdded(def.definition, def)
                } else if (definitionChanges.hasItemUpdated(def.definition)) {
                    definitionChanges.putItemUpdated(def.definition, def)
                }
            }
        }
    }
}
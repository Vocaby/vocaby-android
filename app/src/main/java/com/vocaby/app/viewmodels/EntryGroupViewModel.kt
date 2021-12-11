package com.vocaby.app.viewmodels

import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vocaby.app.Constants
import com.vocaby.app.R
import com.vocaby.app.models.customentry.DefinitionChanges
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import com.vocaby.app.models.dictionary.DefinitionModel
import com.vocaby.app.models.payload.PayloadState
import com.vocaby.app.models.viewstate.TextViewStateModel
import com.vocaby.app.utils.SingleLiveEvent
import java.util.*

class EntryGroupViewModel : ViewModel() {
    private lateinit var definitionGroup: DefinitionGroupModel
    private var definitionChanges: DefinitionChanges = DefinitionChanges()
    private val initialDefinitions: HashMap<String, DefinitionModel> = HashMap()
    private var resultState: Int

    private val _type: SingleLiveEvent<String> = SingleLiveEvent()
    private val _definitions: SingleLiveEvent<List<DefinitionModel>> = SingleLiveEvent()
    private val _definitionsAlert: SingleLiveEvent<TextViewStateModel> = SingleLiveEvent()
    private val _definitionsAddStatus: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val definitions: LiveData<List<DefinitionModel>> get() = _definitions
    val type: LiveData<String> get() = _type
    val definitionAlert: LiveData<TextViewStateModel> get() = _definitionsAlert
    val definitionAddStatus: LiveData<Boolean> get() = _definitionsAddStatus

    init {
        resultState = PayloadState.ADD
    }

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
        resultState = receivedIntent.getIntExtra(Constants.ITEM_PAYLOAD_KEY, PayloadState.UNCHANGED)
    }

    fun removeAlert() {
        _definitionsAlert.value = TextViewStateModel(
            View.GONE,
            android.R.string.cancel
        )
    }

    fun addDefinition(definition: String, example: String?) {
        val alertState: TextViewStateModel

        if (definition.isEmpty()) {
            alertState = TextViewStateModel(
                View.VISIBLE,
                R.string.definition_empty_alert
            )
        } else {
            if (definitionGroup.hasDefinition(definition)) {
                alertState = TextViewStateModel(
                    View.VISIBLE,
                    R.string.definition_exists_alert
                )
            } else {
                alertState = TextViewStateModel(
                    View.GONE,
                    android.R.string.cancel
                )

                var definitionToAdd = initialDefinitions[definition]
                if (definitionToAdd != null) {
                    definitionToAdd = DefinitionModel(definitionToAdd)
                    definitionToAdd.example = example
                    definitionGroup.addNewDefinition(definitionToAdd)
                } else {
                    definitionToAdd = definitionGroup.addNewDefinition(definition, example)
                }

                definitionChanges.addItem(definition, definitionToAdd)
                _definitionsAddStatus.setValue(true)
            }
        }

        _definitionsAlert.value = alertState
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
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, resultState)
        intent.putExtra(EntryViewModel.DEFINITION_CHANGES, definitionChanges)
        intent.putExtra(EntryViewModel.GROUP_KEY, definitionGroup)
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
        // if it's a new group that's being updated
        if (definitionGroup.groupId == -1 && resultState == PayloadState.UPDATE) {
            for (def in definitionGroup.definitionData) {
                if (definitionChanges.hasItemAdded(def.definition)) {
                    definitionChanges.putItemAdded(def.definition, def)
                }
            }
        }
    }
}
package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.EntryBuilderUseCases
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.payloads.ItemEntryPayload
import com.vocaby.application.payloads.ItemStringPayload
import com.vocaby.application.states.ItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.set

@HiltViewModel
class EntryBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDictionaryRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val entryBuilderUseCases: EntryBuilderUseCases
) : ViewModel() {
    companion object {
        const val GROUP_KEY = "GK"
        const val INITIAL_DEFINITIONS_KEY = "IDK"
        const val DEFINITION_CHANGES = "DEFCK"
    }
    private val actionPayload: ItemStringPayload = savedStateHandle.get<ItemStringPayload>(Constants.ITEM_PAYLOAD_KEY)!!
    private lateinit var entryData: EntryModel
    private var initTypes: Map<String, Type> = HashMap()
    private var availableTypes: ArrayList<Type> = ArrayList()
    private var userId: Int = 1
    private var selectedGroup = -1

    private val initialGroups: HashMap<String, DefinitionGroupModel> = HashMap()
    private val groupChanges: ItemChangeState<DefinitionGroupModel> = ItemChangeState()
    private val definitionChangesMap: MutableMap<String, ItemChangeState<DefinitionModel>> = HashMap()
    private var saveTime: Date = Date()

    private val _uiState = MutableStateFlow<EntryBuilderUiState>(EntryBuilderUiState.InProgress)
    private val _uiEvent = MutableSharedFlow<EntryBuilderUiEvent>()

    val uiState get() = _uiState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            userId = userDictionaryRepository.getUser()
            entryData = entryBuilderUseCases.getCustomEntryUseCase(userId, actionPayload.payload)
            _uiState.value = EntryBuilderUiState.UpdateUiState(
                if (actionPayload.state == ItemState.ADD) "New Entry" else "Update Entry",
                entryData.entry,
                entryData.pronunciation ?: "",
                entryData.definitionGroups
            )

            groupChanges.id = entryData.id

            for (group in entryData.definitionGroups) {
                // shallow group copy is fine
                val clone = group.copy()
                initialGroups[clone.type] = clone
                definitionChangesMap[clone.type] = ItemChangeState(clone.groupId)
            }

            // In case user attempts to create a new entry but the entry already exists
            if (!entryData.isEmpty) actionPayload.state = ItemState.UPDATE

            calculateAvailableTypes()
        }
    }

    fun handleDialogResult(bundle: Bundle) {
        val selectedType = bundle.getString(EntryBuilderGroupDialogFragment.SELECTED_TYPE)
        val typesModified = bundle.getBoolean(EntryBuilderGroupDialogFragment.TYPES_CHANGED)

        viewModelScope.launch {
            if (selectedType != null) {
                _uiEvent.emit(EntryBuilderUiEvent.OpenGroupBuilder(selectedType))
            } else {
                if (typesModified) {
                    calculateAvailableTypes()
                }
            }
        }
    }

    private fun calculateAvailableTypes() {
        viewModelScope.launch {
            val list = entryBuilderUseCases.getTypesUseCase().first()
            initTypes = list.associateBy { it.type }
            availableTypes = ArrayList(
                list.filter { entryData.definitionGroups.none { group -> group.type == it.type } }
            )
        }
    }

    fun getAvailableTypes() {
        viewModelScope.launch {
            _uiEvent.emit(EntryBuilderUiEvent.ShowTypeSelectionDialog(ArrayList(availableTypes)))
        }
    }

    fun addExistingGroupDataToIntent(intent: Intent, position: Int): Intent {
        val type = entryData.getDefinitionGroup(position).type

        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position) as Parcelable)
        intent.putExtra(DEFINITION_CHANGES, definitionChangesMap[type])
        intent.putExtra(INITIAL_DEFINITIONS_KEY, initialGroups[type] as Parcelable)
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, ItemState.UPDATE as Parcelable)
        return intent
    }

    fun addNewGroupDataToIntent(intent: Intent, type: String): Intent {
        val newGroup = DefinitionGroupModel(type, order = entryData.definitionGroups.size)

        intent.putExtra(GROUP_KEY, newGroup as Parcelable)
        intent.putExtra(DEFINITION_CHANGES, ItemChangeState<DefinitionModel>())
        intent.putExtra(INITIAL_DEFINITIONS_KEY, newGroup as Parcelable)
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, ItemState.ADD as Parcelable)
        return intent
    }

    fun setSelectedGroup(selectedGroup: Int) {
        this.selectedGroup = selectedGroup
    }

    fun removeGroup(groupDefinitionState: ItemState, position: Int) {
        viewModelScope.launch {
            if (groupDefinitionState == ItemState.UPDATE) {
                val groupRemoved = entryData.removeGroup(position)

                // clear definition changes as well for the removed group
                definitionChangesMap.remove(groupRemoved.type)
                groupChanges.removeItem(groupRemoved.type, groupRemoved)
                val initType = initTypes[groupRemoved.type]
                initType?.let { availableTypes.add(it.order, it) }
                _uiEvent.emit(EntryBuilderUiEvent.UpdateAdapter(position, ItemState.DELETE))
            }
        }
    }

    private fun addGroup(newGroup: DefinitionGroupModel) {
        viewModelScope.launch {
            entryData.addDefinitionGroup(newGroup)
            groupChanges.putItemAdded(newGroup.type, newGroup)
            availableTypes.removeIf {it.type == newGroup.type}
            _uiEvent.emit(EntryBuilderUiEvent.UpdateAdapter(selectedGroup, ItemState.ADD))
        }
    }

    private fun updateGroup(definitionGroup: DefinitionGroupModel) {
        viewModelScope.launch {
            entryData.replaceDefinitionGroup(definitionGroup.type, definitionGroup)
            groupChanges.putItemUpdated(definitionGroup.type, definitionGroup)
            _uiEvent.emit(EntryBuilderUiEvent.UpdateAdapter(selectedGroup, ItemState.UPDATE))
        }
    }

    fun handleGroupCreationResult(result: ActivityResult) {
        result.data?.let { data ->
            if (result.resultCode == Activity.RESULT_OK) {
                val definitionGroup: DefinitionGroupModel? = data.getParcelableExtra(GROUP_KEY)

                definitionGroup?.let {
                    val groupDefinitionState: ItemState = data.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)!!
                    if (definitionGroup.isEmpty) {
                        removeGroup(groupDefinitionState, selectedGroup)
                    } else {
                        if (groupDefinitionState == ItemState.UPDATE) {
                            updateGroup(definitionGroup)
                        } else {
                            addGroup(definitionGroup)
                        }

                        // add/overwrite the definition changes
                        val newChanges: ItemChangeState<DefinitionModel> = data.getParcelableExtra(DEFINITION_CHANGES)!!
                        definitionChangesMap[definitionGroup.type] = newChanges
                    }
                }
            }
        }
    }

    // User saved the entry
    private fun closeBuilder() {
        viewModelScope.launch {
            val intent = Intent()
            val userEntry = UserEntry(actionPayload.payload, saveTime)
            intent.putExtra(Constants.ITEM_PAYLOAD_KEY, ItemEntryPayload(userEntry, actionPayload.state))
            _uiEvent.emit(EntryBuilderUiEvent.CloseBuilder(intent))
        }
    }

    private fun cancelBuilder() {
        viewModelScope.launch {
            _uiEvent.emit(EntryBuilderUiEvent.CancelBuilder)
        }
    }

    // USER CLICKS SAVE
    fun saveUserEntry(pronunciation: String) {
        val pronun = Formatter.cleanText(pronunciation)
        checkForUpdatedItems()

        if (entryData.definitionGroups.isEmpty()) {
            if (actionPayload.state == ItemState.ADD) {
                // NEW ENTRY IS EMPTY SO CANCEL
                cancelBuilder()
            } else {
                // DELETE THE EXISTING ENTRY BECAUSE THE USER DELETED ALL GROUPS
                viewModelScope.launch {
                    customDictionaryRepository.removeUserEntry(entryData.id)
                    actionPayload.state = ItemState.DELETE
                    closeBuilder()
                }
            }
        } else {
            var definitionHasChanges = false
            for (definitionChanges in definitionChangesMap.values) {
                if (definitionChanges.hasChanges()) {
                    definitionHasChanges = true
                    break
                }
            }

            if (!groupChanges.hasChanges() && !definitionHasChanges && entryData.pronunciation == pronun) {
                cancelBuilder()
            } else {
                viewModelScope.launch {
                    saveTime = Date()

                    entryData.id = customDictionaryRepository.insertOrUpdateEntry(
                        userId,
                        entryData.entry,
                        pronun,
                        groupChanges,
                        definitionChangesMap,
                        saveTime
                    )

                    closeBuilder()
                }
            }
        }
    }

    private fun checkForUpdatedItems() {
        if (entryData.definitionGroups.isNotEmpty() && actionPayload.state == ItemState.UPDATE) {
            for (i in entryData.definitionGroups.indices) {
                val currentGroup = entryData.definitionGroups[i]
                val originalGroup = initialGroups[currentGroup.type]

                originalGroup?.let {
                    if (originalGroup.order == currentGroup.order) {
                        groupChanges.removeItemUpdated(currentGroup.type)
                    } else {
                        groupChanges.putItemUpdated(currentGroup.type, currentGroup)
                    }
                }
            }
        }
    }
}
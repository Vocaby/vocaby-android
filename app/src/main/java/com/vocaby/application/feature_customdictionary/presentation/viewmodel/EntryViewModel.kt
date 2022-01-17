package com.vocaby.application.feature_customdictionary.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_customdictionary.domain.model.ItemChangeState
import com.vocaby.application.feature_customdictionary.domain.model.UserEntry
import com.vocaby.application.feature_customdictionary.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.payloads.ItemEntryPayload
import com.vocaby.application.payloads.ItemIntPayload
import com.vocaby.application.payloads.ItemStringPayload
import com.vocaby.application.states.ItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.indices
import kotlin.collections.isNotEmpty
import kotlin.collections.set

@HiltViewModel
class EntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDictionaryRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
) : ViewModel() {
    companion object {
        const val GROUP_KEY = "GK"
        const val INITIAL_DEFINITIONS_KEY = "IDK"
        const val DEFINITION_CHANGES = "DEFCK"
    }

    private val payload: ItemStringPayload = savedStateHandle.get<ItemStringPayload>(Constants.ITEM_PAYLOAD_KEY)!!
    private lateinit var entryData: EntryModel
    private var userId: Int = 1
    private var selectedGroup = -1
    private val initialGroups: HashMap<String, DefinitionGroupModel> = HashMap()
    private val groupChanges: ItemChangeState<DefinitionGroupModel> = ItemChangeState()
    private val definitionChangesMap: MutableMap<String, ItemChangeState<DefinitionModel>> = HashMap()
    private var saveTime: Date = Date()

    private val _entry: SingleLiveEvent<String> = SingleLiveEvent()
    private val _pronunciation: SingleLiveEvent<String> = SingleLiveEvent()
    private val _groupChange: SingleLiveEvent<ItemIntPayload> = SingleLiveEvent()
    private val _typeChange: SingleLiveEvent<ItemStringPayload> = SingleLiveEvent()
    private val _definitionGroups: SingleLiveEvent<List<DefinitionGroupModel>> = SingleLiveEvent()
    private val _saveResult: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _types: SingleLiveEvent<MutableList<String>> = SingleLiveEvent()
    private val _selectedType: SingleLiveEvent<String> = SingleLiveEvent()
    private val _editorState: SingleLiveEvent<ItemState> = SingleLiveEvent()

    val entry: LiveData<String> get() = _entry
    val pronunciation: LiveData<String> get() = _pronunciation
    val definitionGroups: LiveData<List<DefinitionGroupModel>> get() = _definitionGroups
    val groupChange: LiveData<ItemIntPayload> get() = _groupChange
    val typeChange: LiveData<ItemStringPayload> get() = _typeChange
    val saveResult: LiveData<Boolean> get() = _saveResult
    val types: LiveData<MutableList<String>> get() = _types
    val selectedType: LiveData<String> get() = _selectedType
    val editorState: LiveData<ItemState> get() = _editorState

    init {
        viewModelScope.launch(Dispatchers.Default) {
            userId = userDictionaryRepository.getUser()
            val types = customDictionaryRepository.getTypes() as MutableList<String>

            val data = customDictionaryRepository.getUserEntryData(userId, payload.payload)
            entryData = data ?: EntryModel(payload.payload)

            _entry.postValue(entryData.entry)
            _definitionGroups.postValue(entryData.definitionGroups)
            _pronunciation.postValue(entryData.pronunciation)
            groupChanges.id = entryData.id

            for (group in entryData.definitionGroups) {
                val clone = DefinitionGroupModel(group)
                initialGroups[clone.type] = clone
                definitionChangesMap[clone.type] = ItemChangeState(clone.groupId)
                types.remove(clone.type)
            }

            _types.postValue(types)

            if (payload.state == ItemState.ADD) {
                _editorState.postValue(ItemState.ADD)
            } else if (payload.state == ItemState.UPDATE) {
                _editorState.postValue(ItemState.UPDATE)
            }

            // In case user attempts to create a new entry but the entry already exists
            if (!entryData.isEmpty) payload.state = ItemState.UPDATE
        }

        _selectedType.value = ""
    }

    fun repopulateUI() {
        _entry.value = entryData.entry
        _definitionGroups.value = entryData.definitionGroups
        _pronunciation.value = entryData.pronunciation
        _types.value = _types.value
        _editorState.value = _editorState.value
        _selectedType.value = ""
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
        val newGroup = DefinitionGroupModel(type)
        initialGroups[type] = newGroup
        intent.putExtra(GROUP_KEY, newGroup as Parcelable)
        intent.putExtra(
            DEFINITION_CHANGES,
            ItemChangeState<DefinitionModel>()
        )
        intent.putExtra(INITIAL_DEFINITIONS_KEY, newGroup as Parcelable)
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, ItemState.ADD as Parcelable)
        return intent
    }

    fun setSelectedGroup(selectedGroup: Int) {
        this.selectedGroup = selectedGroup
    }

    fun removeGroup(groupDefinitionState: ItemState, position: Int) {
        if (groupDefinitionState == ItemState.UPDATE) {
            val groupRemoved = entryData.removeGroup(position)

            // clear definition changes as well for the removed group
            definitionChangesMap.remove(groupRemoved.type)
            groupChanges.removeItem(groupRemoved.type, groupRemoved)
            _groupChange.value = ItemIntPayload(position, ItemState.DELETE)
            _typeChange.value = ItemStringPayload(groupRemoved.type, ItemState.ADD)
        }
    }

    private fun addGroup(newGroup: DefinitionGroupModel) {
        entryData.addDefinitionGroup(newGroup)
        groupChanges.putItemAdded(newGroup.type, newGroup)
        _groupChange.value = ItemIntPayload(selectedGroup, ItemState.ADD)
        _typeChange.value = ItemStringPayload(newGroup.type, ItemState.DELETE)
        _selectedType.value = ""
    }

    fun handleGroupCreationResult(result: ActivityResult) {
        result.data?.let { data ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (data.getParcelableExtra<Parcelable>(GROUP_KEY) is DefinitionGroupModel) {
                    val definitionGroup: DefinitionGroupModel? = data.getParcelableExtra(GROUP_KEY)

                    definitionGroup?.let {
                        val type: String = definitionGroup.type
                        val groupDefinitionState: ItemState? = data.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)

                        if (definitionGroup.isEmpty) {
                            removeGroup(groupDefinitionState!!, selectedGroup)
                        } else {
                            if (groupDefinitionState == ItemState.UPDATE) {
                                entryData.replaceDefinitionGroup(type, definitionGroup)
                                groupChanges.putItemUpdated(definitionGroup.type, definitionGroup)
                                _groupChange.setValue(ItemIntPayload(selectedGroup, ItemState.UPDATE))
                            } else {
                                addGroup(definitionGroup)
                            }

                            // add/overwrite the definition changes
                            val newChanges: ItemChangeState<DefinitionModel>? = data.getParcelableExtra(
                                DEFINITION_CHANGES
                            )

                            definitionChangesMap[type] = newChanges!!
                        }
                    }
                }
            }
        }
    }

    // User saved the entry
    fun addEntryResultDataToIntent(): Intent {
        val intent = Intent()
        val userEntry = UserEntry(payload.payload, saveTime)
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, ItemEntryPayload(userEntry, payload.state))
        return intent
    }

    // USER CLICKS SAVE
    fun saveUserEntry(pronunciation: String) {
        val pronun = Formatter.cleanText(pronunciation)
        checkForUpdatedItems()

        if (entryData.definitionGroups.isEmpty()) {
            if (payload.state == ItemState.ADD) {
                // NEW ENTRY IS EMPTY SO CANCEL
                _saveResult.setValue(false)
            } else {
                // DELETE THE EXISTING ENTRY BECAUSE THE USER DELETED ALL GROUPS
                viewModelScope.launch {
                    customDictionaryRepository.removeCustomEntry(entryData.id)
                    payload.state = ItemState.DELETE
                    _saveResult.postValue(true)
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
                _saveResult.setValue(false)
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

                    _saveResult.postValue(true)
                }
            }
        }
    }

    private fun checkForUpdatedItems() {
        if (entryData.definitionGroups.isNotEmpty() && payload.state == ItemState.UPDATE) {
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

    fun setSelectedType(type: String?) {
        _selectedType.value = type
    }
}
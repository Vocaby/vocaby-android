package com.vocaby.app.viewmodels

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.app.Constants
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.models.customentry.DefinitionChanges
import com.vocaby.app.models.customentry.GroupChanges
import com.vocaby.app.models.customentry.UserEntry
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.payloads.ItemEntryPayload
import com.vocaby.app.payloads.ItemIntPayload
import com.vocaby.app.payloads.ItemStringPayload
import com.vocaby.app.states.ItemState
import com.vocaby.app.utils.Formatter
import com.vocaby.app.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EntryViewModel(
    private val repository: VocabyRepository,
    private val payload: ItemStringPayload
) : ViewModel() {
    companion object {
        const val GROUP_KEY = "GK"
        const val INITIAL_DEFINITIONS_KEY = "IDK"
        const val DEFINITION_CHANGES = "DEFCK"
    }

    private lateinit var entryData: EntryModel
    private var selectedGroup = -1
    private val initialGroups: HashMap<String, DefinitionGroupModel> = HashMap()
    private val groupChanges: GroupChanges = GroupChanges(-1)
    private val definitionChangesMap: MutableMap<String, DefinitionChanges> = HashMap()
    private var saveTime: String = Formatter.formatDateToString(Date().time)

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
            val types = repository.getTypes() as MutableList<String>

            val data = repository.getUserEntryData(payload.payload)
            entryData = data ?: EntryModel(payload.payload)

            _entry.postValue(entryData.entry)
            _definitionGroups.postValue(entryData.definitionGroups)
            _pronunciation.postValue(entryData.pronunciation)
            groupChanges.entryId = entryData.id

            for (group in entryData.definitionGroups) {
                val clone = DefinitionGroupModel(group)
                initialGroups[clone.type] = clone
                definitionChangesMap[clone.type] = DefinitionChanges(clone.groupId)
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
        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position))
        intent.putExtra(DEFINITION_CHANGES, definitionChangesMap[type])
        intent.putExtra(INITIAL_DEFINITIONS_KEY, initialGroups[type])
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, ItemState.UPDATE as Parcelable)
        return intent
    }

    fun addNewGroupDataToIntent(intent: Intent, type: String): Intent {
        val newGroup = DefinitionGroupModel(type)
        initialGroups[type] = newGroup
        intent.putExtra(GROUP_KEY, newGroup)
        intent.putExtra(DEFINITION_CHANGES, DefinitionChanges())
        intent.putExtra(INITIAL_DEFINITIONS_KEY, newGroup)
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
                            val newChanges: DefinitionChanges? = data.getParcelableExtra(
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
                    repository.removeCustomEntry(entryData.id)
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

            if (!groupChanges.hasChanges() && !definitionHasChanges) {
                _saveResult.setValue(false)
            } else {
                viewModelScope.launch {
                    saveTime = Formatter.formatDateToString(Date().time)

                    entryData.id = repository.insertOrUpdateEntry(
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

class EntryViewModelFactory(
    private val repository: VocabyRepository,
    private val payload: ItemStringPayload
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(repository, payload) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
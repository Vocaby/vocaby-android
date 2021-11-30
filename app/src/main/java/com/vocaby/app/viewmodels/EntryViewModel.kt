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
import com.vocaby.app.data.VocabyRepositoryKt
import com.vocaby.app.models.customentry.DefinitionChanges
import com.vocaby.app.models.customentry.GroupChanges
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.models.payload.ItemIntPayload
import com.vocaby.app.models.payload.ItemStringPayload
import com.vocaby.app.models.payload.PayloadState
import com.vocaby.app.utils.SingleLiveEvent
import com.vocaby.app.utils.StringFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EntryViewModel(
    private val repository: VocabyRepositoryKt,
    private val payload: ItemStringPayload?
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

    private val _entry: SingleLiveEvent<String> = SingleLiveEvent()
    private val _pronunciation: SingleLiveEvent<String> = SingleLiveEvent()
    private val _groupChange: SingleLiveEvent<ItemIntPayload> = SingleLiveEvent()
    private val _typeChange: SingleLiveEvent<ItemStringPayload> = SingleLiveEvent()
    private val _definitionGroups: SingleLiveEvent<List<DefinitionGroupModel>> = SingleLiveEvent()
    private val _saveResult: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _types: SingleLiveEvent<List<String>> = SingleLiveEvent()
    private val _selectedType: SingleLiveEvent<String> = SingleLiveEvent()

    val entry: LiveData<String> get() = _entry
    val pronunciation: LiveData<String> get() = _pronunciation
    val definitionGroups: LiveData<List<DefinitionGroupModel>> get() = _definitionGroups
    val groupChange: LiveData<ItemIntPayload> get() = _groupChange
    val typeChange: LiveData<ItemStringPayload> get() = _typeChange
    val saveResult: LiveData<Boolean> get() = _saveResult
    val types: LiveData<List<String>?> get() = _types
    val selectedType: LiveData<String> get() = _selectedType


    init {
        viewModelScope.launch(Dispatchers.Default) {
            val types = repository.getTypes() as MutableList<String>

            payload?.let { p ->
                val data = repository.getUserEntryData(p.payload)
                entryData = data ?: EntryModel(p.payload)

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

                // In case user attempts to create a new entry but the entry already exists
                if (!entryData.isEmpty) p.state = PayloadState.UPDATE
            }
        }

        _selectedType.value = ""
    }

    fun addExistingGroupDataToIntent(intent: Intent, position: Int): Intent {
        val type = entryData.getDefinitionGroup(position).type
        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position))
        intent.putExtra(DEFINITION_CHANGES, definitionChangesMap[type])
        intent.putExtra(INITIAL_DEFINITIONS_KEY, initialGroups[type])
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, PayloadState.UPDATE)
        return intent
    }

    fun addNewGroupDataToIntent(intent: Intent, type: String?): Intent {
        val newGroup = DefinitionGroupModel(type, entryData.definitionGroups.size - 1)
        intent.putExtra(GROUP_KEY, newGroup)
        intent.putExtra(DEFINITION_CHANGES, DefinitionChanges())
        intent.putExtra(INITIAL_DEFINITIONS_KEY, newGroup)
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, PayloadState.ADD)
        return intent
    }

    fun setSelectedGroup(selectedGroup: Int) {
        this.selectedGroup = selectedGroup
    }

    fun removeGroup(groupDefinitionState: Int, position: Int) {
        if (groupDefinitionState == PayloadState.UPDATE) {
            val groupRemoved = entryData.removeGroup(position)

            // clear definition changes as well for the removed group
            definitionChangesMap.remove(groupRemoved.type)
            groupChanges.removeItem(groupRemoved.type, groupRemoved)
            _groupChange.value = ItemIntPayload(PayloadState.DELETE, position)
            _typeChange.value = ItemStringPayload(PayloadState.ADD, groupRemoved.type)
        }
    }

    private fun addGroup(newGroup: DefinitionGroupModel?) {
        entryData.addDefinitionGroup(newGroup)
        groupChanges.putItemAdded(newGroup!!.type, newGroup)
        _groupChange.value = ItemIntPayload(PayloadState.ADD, selectedGroup)
        _typeChange.value = ItemStringPayload(PayloadState.DELETE, newGroup.type)
    }

    fun handleGroupCreationResult(result: ActivityResult) {
        result.data?.let { data ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (data.getParcelableExtra<Parcelable>(GROUP_KEY) is DefinitionGroupModel) {
                    val definitionGroup: DefinitionGroupModel? = data.getParcelableExtra(GROUP_KEY)

                    definitionGroup?.let {
                        val type: String = definitionGroup.type
                        val groupDefinitionState = data.getIntExtra(Constants.ITEM_PAYLOAD_KEY, PayloadState.ADD)

                        if (definitionGroup.isEmpty) {
                            removeGroup(groupDefinitionState, selectedGroup)
                        } else {
                            if (groupDefinitionState == PayloadState.UPDATE) {
                                entryData.replaceDefinitionGroup(type, definitionGroup)
                                groupChanges.putItemUpdated(definitionGroup.type, definitionGroup)
                                _groupChange.setValue(ItemIntPayload(PayloadState.UPDATE, selectedGroup))
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
        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, payload)
        return intent
    }

    // USER CLICKS SAVE
    fun saveUserEntry(pronunciation: String) {
        val pronun = StringFormatter.cleanText(pronunciation)
        checkForUpdatedItems()

        if (entryData.definitionGroups.isEmpty()) {
            if (payload?.state == PayloadState.ADD) {
                // NEW ENTRY IS EMPTY SO CANCEL
                _saveResult.setValue(false)
            } else {
                // DELETE THE EXISTING ENTRY BECAUSE THE USER DELETED ALL GROUPS
                viewModelScope.launch {
                    repository.removeCustomEntry(entryData.id, entryData.entry)
                    payload?.state = PayloadState.DELETE
                    _saveResult.postValue(true)
                }
            }
        } else {
            viewModelScope.launch {
                entryData.id = repository.insertOrUpdateEntry(
                    entryData.entry,
                    pronun,
                    groupChanges,
                    definitionChangesMap
                )

                _saveResult.postValue(true)
            }
        }
    }

    private fun checkForUpdatedItems() {
        if (entryData.definitionGroups.isNotEmpty() && payload?.state == PayloadState.UPDATE) {
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
    private val repository: VocabyRepositoryKt,
    private val payload: ItemStringPayload?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(repository, payload) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
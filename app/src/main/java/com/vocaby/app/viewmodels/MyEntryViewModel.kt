package com.vocaby.app.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.*
import com.vocaby.app.Constants
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.states.ItemIntPayload
import com.vocaby.app.states.ItemState
import com.vocaby.app.states.ItemStringPayload
import com.vocaby.app.states.UserInputState
import com.vocaby.app.utils.SingleLiveEvent
import com.vocaby.app.utils.StringFormatter
import kotlinx.coroutines.launch
import java.util.*

class MyEntryViewModel(private val repository: VocabyRepository): ViewModel() {
    private val _entryCount: MutableLiveData<Int> = MutableLiveData(0)
    private val _entries: SingleLiveEvent<List<String>> = SingleLiveEvent()
    private val _entryState: SingleLiveEvent<ItemIntPayload> = SingleLiveEvent()
    private val _userInput: SingleLiveEvent<UserInputState> = SingleLiveEvent()

    private var selectedPosition: Int = -1
    private var customEntries: MutableList<String> = ArrayList()

    val entries: LiveData<List<String>>
        get() = _entries
    val customEntryCount: LiveData<Int>
        get() = _entryCount
    val entryResult: LiveData<ItemIntPayload>
        get() = _entryState
    val entryCreationState: LiveData<UserInputState>
        get() = _userInput

    init {
        // populate user entries
        initializeEntries()
    }

    fun clearEntries() {
        viewModelScope.launch {
            repository.clearUserEntries()
            customEntries = ArrayList()
            _entries.postValue(customEntries)
            _entryCount.postValue(0)
        }
    }

    fun addEntryDataToIntent(intent: Intent, entry: String, position: Int): Intent {
        val itemPayload = ItemStringPayload(entry, ItemState.ADD)
        selectedPosition = if (position == -1) {
            customEntries.indexOf(entry)
        } else {
            position
        }

        if (selectedPosition != -1) {
            itemPayload.state = ItemState.UPDATE
        }

        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, itemPayload)
        return intent
    }

    fun handleResult(result: ActivityResult) {
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val receivedPayload: ItemStringPayload? =
                result.data!!.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)

            receivedPayload?.let {
                if (receivedPayload.state == ItemState.ADD) {
                    customEntries.add(0, receivedPayload.payload)
                    _entryState.value = ItemIntPayload(0, ItemState.ADD)
                } else if (receivedPayload.state == ItemState.DELETE && selectedPosition != -1) {
                    customEntries.removeAt(selectedPosition)
                    _entryState.value = ItemIntPayload(selectedPosition, ItemState.DELETE)
                }

                _entryCount.value = customEntries.size
            }
        }
    }

    fun removeCustomEntry(entry: String, position: Int) {
        viewModelScope.launch {
            repository.removeCustomEntry(entry)
            customEntries.removeAt(position)
            _entryCount.postValue(customEntries.size)
            _entryState.value = ItemIntPayload(position, ItemState.DELETE)
        }
    }

    fun createCustomEntry(entry:String) {
        if (entry.isEmpty()) {
            _userInput.value = UserInputState.EmptyInput
        } else if (StringFormatter.containsSpecialCharacter(entry)) {
            _userInput.value = UserInputState.InvalidInput
        } else {
            val sanitizedEntry = StringFormatter.cleanText(entry)
            if (sanitizedEntry.length > Constants.ENTRY_MAX_LENGTH) {
                _userInput.value = UserInputState.LongInput
            } else {
                _userInput.value = UserInputState.Valid(sanitizedEntry)
            }
        }
    }

    fun initializeEntries() {
        viewModelScope.launch {
            customEntries = repository.getUserEntries() as MutableList<String>
            _entries.postValue(customEntries)
            _entryCount.postValue(customEntries.size)
        }
    }

    fun reinitializeEntries() {
        _entries.postValue(customEntries)
        _entryCount.postValue(customEntries.size)
    }
}

class MyEntryViewModelFactory(
    private val repository: VocabyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
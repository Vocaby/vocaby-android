package com.vocaby.application.feature_dictionary_custom.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.payloads.ItemEntryPayload
import com.vocaby.application.payloads.ItemIntPayload
import com.vocaby.application.payloads.ItemStringPayload
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MyEntryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository
): ViewModel() {
    private val _entryCount: MutableLiveData<Int> = MutableLiveData(0)
    private val _entries: SingleLiveEvent<GenericState<LinkedList<UserEntry>>> = SingleLiveEvent()
    private val _entryState: SingleLiveEvent<ItemIntPayload> = SingleLiveEvent()
    private val _userInput: SingleLiveEvent<UserInputState> = SingleLiveEvent()

    private var realPosition: Int = -1
    private var filteredPosition: Int = -1
    private var customEntries: LinkedList<UserEntry> = LinkedList()
    private var filteredEntries: LinkedList<UserEntry> = LinkedList()
    private var filtered: Boolean = false

    val entries: LiveData<GenericState<LinkedList<UserEntry>>>
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
            val userId = userRepository.getUser()
            customDictionaryRepository.clearUserEntries(userId)
            customEntries = LinkedList()
            _entries.postValue(GenericState.Success(customEntries))
            _entryCount.postValue(0)
        }
    }

    fun filterEntries(newQuery: String) {
        if (newQuery.isEmpty()) {
            filtered = false
            _entries.value = GenericState.Success(customEntries)
        } else {
            filtered = true
            filteredEntries = LinkedList()
            val query = newQuery.lowercase()
            for (entry in customEntries) {
                if (entry.entry.startsWith(query)) {
                    filteredEntries.add(entry)
                }
            }

            _entries.value = GenericState.Success(filteredEntries)
        }
    }

    // When it's -1, then a new entry is being created
    // when it's not -1, then an entry is being updated
    fun addEntryDataToIntent(intent: Intent, entry: String, position: Int = -1): Intent {
        val itemPayload = ItemStringPayload(entry, ItemState.ADD)

        if (position != -1) {
            itemPayload.state = ItemState.UPDATE
            if (filtered) {
                setRealPosition(entry)
                filteredPosition = position
            } else {
                realPosition = position
            }
        }

        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, itemPayload)
        return intent
    }

    fun resetSelections() {
        realPosition = -1
        filteredPosition = -1
    }

    private fun setRealPosition(entry: String) {
        if (realPosition == -1) {
            for ((index, userEntry) in customEntries.withIndex()) {
                if (userEntry.entry == entry) {
                    realPosition = index
                }
            }
        }
    }

    fun handleResult(result: ActivityResult) {
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val receivedPayload: ItemEntryPayload? =
                result.data!!.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)

            receivedPayload?.let {
                if (receivedPayload.state == ItemState.ADD) {
                    if (filtered) filteredEntries.add(0, receivedPayload.payload)
                    customEntries.add(0, receivedPayload.payload)
                } else if (receivedPayload.state == ItemState.DELETE && realPosition != -1) {
                    if (filtered) filteredEntries.removeAt(filteredPosition)
                    customEntries.removeAt(realPosition)
                } else if (receivedPayload.state == ItemState.UPDATE) {
                    if (filtered) {
                        filteredEntries.removeAt(filteredPosition)
                        filteredEntries.add(0, receivedPayload.payload)
                    }
                    customEntries.removeAt(realPosition)
                    customEntries.add(0, receivedPayload.payload)
                }

                if (filtered) {
                    _entryState.value = ItemIntPayload(filteredPosition, receivedPayload.state)
                } else {
                    _entryState.value = ItemIntPayload(realPosition, receivedPayload.state)
                }

                _entryCount.value = customEntries.size
            }
        }

        resetSelections()
    }

    fun removeCustomEntry(entry: String, position: Int) {
        viewModelScope.launch {
            customDictionaryRepository.removeCustomEntry(entry)
            if (filtered) {
                setRealPosition(entry)

                filteredEntries.removeAt(position)
                customEntries.removeAt(realPosition)
            } else {
                customEntries.removeAt(position)
            }

            _entryCount.postValue(customEntries.size)
            _entryState.value = ItemIntPayload(position, ItemState.DELETE)
        }
    }

    fun createCustomEntry(entry:String) {
        if (entry.isEmpty()) {
            _userInput.value = UserInputState.EmptyInput
        } else if (Formatter.containsSpecialCharacter(entry)) {
            _userInput.value = UserInputState.InvalidInput
        } else {
            val sanitizedEntry = Formatter.cleanText(entry)
            if (sanitizedEntry.length > Constants.ENTRY_MAX_LENGTH) {
                _userInput.value = UserInputState.LongInput
            } else {
                setRealPosition(sanitizedEntry)
                if (realPosition != -1) {
                    _userInput.value = UserInputState.SameInput
                } else {
                    _userInput.value = UserInputState.Valid(sanitizedEntry)
                }
            }
        }
    }

    fun initializeEntries() {
        _entries.value = GenericState.InProgress
        viewModelScope.launch {
            val userId = userRepository.getUser()
            customEntries = customDictionaryRepository.getUserEntries(userId)
            _entries.postValue(GenericState.Success(customEntries))
            _entryCount.postValue(customEntries.size)
        }
    }

    fun reinitializeEntries() {
        _entries.postValue(GenericState.Success(customEntries))
        _entryCount.postValue(customEntries.size)
    }
}
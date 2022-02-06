package com.vocaby.application.feature_dictionary_custom.presentation.home

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.Logger
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.use_case.home.CustomEntryUseCases
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.payloads.ItemEntryPayload
import com.vocaby.application.payloads.ItemStringPayload
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MyEntryViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val customEntryUseCases: CustomEntryUseCases,
): ViewModel() {
    private val _uiState = MutableStateFlow<CustomEntryUiState>(CustomEntryUiState.InProgress)
    private val _uiEvent = MutableSharedFlow<CustomEntryUiEvent>()

    private var currentUser: Int? = null
    private var realPosition: Int = -1
    private var entries: LinkedList<UserEntry> = LinkedList()
    private var filteredPosition: Int = -1
    private var filteredEntries: LinkedList<UserEntry> = LinkedList()
    private var isFilterDisplayed: Boolean = false
    private var filteredQuery: String = ""

    val uiState get() = _uiState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect {
                currentUser = it
                entries = customEntryUseCases.getCustomEntriesUseCase(it)
                _uiState.value = CustomEntryUiState.UpdateEntries(entries, getCount())
            }
        }
    }

    private fun resetFilter() {
        filteredQuery = ""
        isFilterDisplayed = false
        filteredEntries = LinkedList()
    }

    fun watchFilter(oldQuery: String, newQuery: String) {
        viewModelScope.launch {
            if (newQuery.isEmpty()) {
                resetFilter()
                _uiState.value = CustomEntryUiState.UpdateEntries(entries, getCount())
            }
        }
    }

    fun filterEntries(newQuery: String) {
        val newFilter = newQuery.lowercase().trim()
        if (filteredQuery != newFilter) {
            _uiState.value = CustomEntryUiState.InProgress
            viewModelScope.launch(Dispatchers.Default) {
                if (newFilter.isEmpty()) {
                    resetFilter()
                    _uiState.value = CustomEntryUiState.UpdateEntries(entries, getCount())
                } else {
                    isFilterDisplayed = true
                    currentUser?.let {
                        filteredEntries = customEntryUseCases.filterCustomEntriesUseCase(it, newFilter)
                    }

                    filteredQuery = newFilter
                    _uiState.value = CustomEntryUiState.UpdateEntries(filteredEntries, getCount())
                }
            }
        }
    }

    fun handleResult(result: ActivityResult) {
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val payload: ItemEntryPayload? = result.data!!.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)
            viewModelScope.launch {
                payload?.let {
                    val containsFilterQuery = payload.payload.entry.startsWith(filteredQuery)
                    val updateFilteredList = containsFilterQuery && isFilterDisplayed

                    if (payload.state == ItemState.ADD) {
                        if (updateFilteredList) filteredEntries.add(0, payload.payload)
                        entries.add(0, payload.payload)
                    } else if (payload.state == ItemState.DELETE && realPosition != -1) {
                        if (updateFilteredList) filteredEntries.removeAt(filteredPosition)
                        entries.removeAt(realPosition)
                    } else if (payload.state == ItemState.UPDATE) {
                        if (updateFilteredList) {
                            filteredEntries.removeAt(filteredPosition)
                            filteredEntries.add(0, payload.payload)
                        }

                        entries.removeAt(realPosition)
                        entries.add(0, payload.payload)
                    }

                    if (isFilterDisplayed) {
                        if (containsFilterQuery) {
                            _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(filteredPosition, payload.state))
                        }
                    } else {
                        _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(realPosition, payload.state))
                    }

                    _uiState.value = CustomEntryUiState.UpdateCount(getCount())
                    resetSelections()
                }
            }
        }
    }

    fun removeCustomEntry(sanitizedEntry: String, position: Int) {
        viewModelScope.launch {
            customEntryUseCases.removeCustomEntryUseCase(sanitizedEntry)
            if (isFilterDisplayed) {
                // user removed from filtered list
                ensureRealPosition(sanitizedEntry)
                filteredEntries.removeAt(position)
                entries.removeAt(realPosition)
            } else {
                entries.removeAt(position)
            }

            _uiState.value = CustomEntryUiState.UpdateCount(getCount())
            _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(position, ItemState.DELETE))
            resetSelections()
        }
    }

    fun createCustomEntry(entry:String) {
        val sanitizedEntry = Formatter.cleanText(entry)
        viewModelScope.launch {
            when(val event = customEntryUseCases.validateCustomEntryUseCase(sanitizedEntry, entries)) {
                is UserInputState.SameInput<*> -> {
                    if (event.data is Int){
                        realPosition = event.data
                        if (isFilterDisplayed) filteredPosition = filteredEntries.indexOfFirst { it.entry == entry }
                        _uiEvent.emit(CustomEntryUiEvent.StartEntryBuilder(sanitizedEntry, realPosition))
                    }
                }
                is UserInputState.LongInput -> {
                    _uiEvent.emit(CustomEntryUiEvent.ShowAlert("This entry is too long"))
                }
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(CustomEntryUiEvent.ShowAlert("Please enter a word or phrase"))
                }
                is UserInputState.NoInput -> {
                    _uiEvent.emit(CustomEntryUiEvent.ShowAlert("Please enter a new name"))
                }
                is UserInputState.InvalidInput -> {
                    _uiEvent.emit(CustomEntryUiEvent.ShowAlert("The entry contains special characters"))
                }
                is UserInputState.Valid<*> -> {
                    if (event.data is String){
                        _uiEvent.emit(CustomEntryUiEvent.StartEntryBuilder(event.data))
                        resetSelections()
                    }
                }
            }
        }
    }

    fun addEntryDataToIntentForBuilder(intent: Intent, sanitizedEntry: String, position: Int): Intent {
        val itemPayload = ItemStringPayload(sanitizedEntry, ItemState.ADD)

        if (position != -1) {
            itemPayload.state = ItemState.UPDATE
            if (isFilterDisplayed) {
                ensureRealPosition(sanitizedEntry)
                filteredPosition = position
            } else {
                realPosition = position
            }
        }

        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, itemPayload)
        return intent
    }

    fun clearUserEntries() {
        viewModelScope.launch {
            currentUser?.let {
                customEntryUseCases.removeUserEntriesUseCase(it)
                entries = LinkedList()
                filteredEntries = LinkedList()
                _uiState.value = CustomEntryUiState.UpdateEntries(entries, getCount())
            }
        }
    }

    private fun getCount(): String = if (isFilterDisplayed) {
        Formatter.cleanNumber(filteredEntries.size, "Entry", "Entries")
    } else {
        Formatter.cleanNumber(entries.size, "Entry", "Entries")
    }

    private fun resetSelections() {
        realPosition = -1
        filteredPosition = -1
    }

    private fun ensureRealPosition(entry: String) {
        if (realPosition == -1) {
            realPosition = entries.indexOfFirst { it.entry == entry }
        }
    }
}
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
import com.vocaby.application.feature_dictionary_custom.domain.use_case.CustomEntryUseCases
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

    private var realPosition: Int = -1
    private var entries: LinkedList<UserEntry> = LinkedList()
    private var filteredPosition: Int = -1
    private var filteredEntries: LinkedList<UserEntry> = LinkedList()
    private var isFilterDisplayed: Boolean = false
    private var filterQuery: String = ""

    val uiState get() = _uiState.asStateFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect {
                entries = customEntryUseCases.getCustomEntriesUseCase(it)
                _uiState.value = CustomEntryUiState.ShowEntries(entries, getCount())
            }
        }
    }

    fun filterEntries(newQuery: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (newQuery.isEmpty()) {
                filterQuery = ""
                isFilterDisplayed = false
                filteredEntries = LinkedList()
                _uiState.value = CustomEntryUiState.ShowEntries(entries, getCount())
            } else {
                isFilterDisplayed = true
                filteredEntries = LinkedList()
                filterQuery = newQuery.lowercase()
                for (entry in entries) {
                    if (entry.entry.startsWith(filterQuery)) {
                        filteredEntries.add(entry.copy())
                    }
                }

                _uiState.value = CustomEntryUiState.ShowEntries(filteredEntries, getCount())
            }
        }
    }

    fun addEntryDataToIntent(intent: Intent, entry: String, position: Int): Intent {
        val itemPayload = ItemStringPayload(entry, ItemState.ADD)

        if (position != -1) {
            itemPayload.state = ItemState.UPDATE
            if (isFilterDisplayed) {
                setRealPosition(entry)
                filteredPosition = position
            } else {
                realPosition = position
            }
        }

        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, itemPayload)
        return intent
    }

    fun handleResult(result: ActivityResult) {
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val payload: ItemEntryPayload? = result.data!!.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)

            viewModelScope.launch {
                payload?.let {
                    val shouldUpdateFilter = payload.payload.entry.startsWith(filterQuery)
                    val updateFiltered = shouldUpdateFilter && isFilterDisplayed

                    if (payload.state == ItemState.ADD) {
                        if (updateFiltered) filteredEntries.add(0, payload.payload)
                        entries.add(0, payload.payload)
                    } else if (payload.state == ItemState.DELETE && realPosition != -1) {
                        if (updateFiltered) filteredEntries.removeAt(filteredPosition)
                        entries.removeAt(realPosition)
                    } else if (payload.state == ItemState.UPDATE) {
                        if (updateFiltered) {
                            filteredEntries.removeAt(filteredPosition)
                            filteredEntries.add(0, payload.payload)
                        }

                        entries.removeAt(realPosition)
                        entries.add(0, payload.payload)
                    }

                    if (isFilterDisplayed) {
                        if (shouldUpdateFilter) {
                            Logger.reportToDebug("Update filtered")
                            _uiEvent.emit(
                                CustomEntryUiEvent.UpdateAdapter(filteredPosition, payload.state)
                            )
                            _uiState.value = CustomEntryUiState.UpdateCount(getCount())
                        } else {
                            Logger.reportToDebug("Do nothing")
                        }
                    } else {
                        Logger.reportToDebug("Update all")
                        _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(realPosition, payload.state))
                        _uiState.value = CustomEntryUiState.UpdateCount(getCount())
                    }

                    resetSelections()
                }
            }
        }
    }

    fun removeCustomEntry(entry: String, position: Int) {
        viewModelScope.launch {
            customEntryUseCases.removeCustomEntryUseCase(entry)
            if (isFilterDisplayed) {
                setRealPosition(entry)
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
        viewModelScope.launch {
            when(val event = customEntryUseCases.validateCustomEntryUseCase(entry, entries)) {
                is UserInputState.SameInput<*> -> {
                    if (event.data is Int){
                        realPosition = event.data
                        _uiEvent.emit(CustomEntryUiEvent.StartEntryBuilder(entries[realPosition].entry, realPosition))
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

    fun clearUserEntries() {
        viewModelScope.launch {
            val userId = getCurrentUserUseCase().first()
            customEntryUseCases.removeUserEntriesUseCase(userId)
            entries = LinkedList()
            filteredEntries = LinkedList()
            _uiState.value = CustomEntryUiState.ShowEntries(
                if (isFilterDisplayed) filteredEntries else entries,
                getCount()
            )
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

    private fun setRealPosition(entry: String) {
        if (realPosition == -1) {
            for ((index, userEntry) in entries.withIndex()) {
                if (userEntry.entry == entry) {
                    realPosition = index
                }
            }
        }
    }
}
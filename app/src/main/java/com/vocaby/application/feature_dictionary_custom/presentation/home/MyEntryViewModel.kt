package com.vocaby.application.feature_dictionary_custom.presentation.home

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.Constants
import com.vocaby.application.core.payloads.ItemEntryPayload
import com.vocaby.application.core.states.ItemState
import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary_custom.common.Constants.ENTRY_LIMIT
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.use_case.home.CustomEntryUseCases
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyEntryViewModel @Inject constructor(
    private val customEntryUseCases: CustomEntryUseCases,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
): ViewModel() {
    private var isLoading:Boolean = false
    private var entryCount:Int = 0
    private var realPosition: Int = -1
    private var entries: LinkedList<UserEntry?> = LinkedList()
    private var filteredPosition: Int = -1
    private var filteredEntries: LinkedList<UserEntry?> = LinkedList()
    private var isFilterDisplayed: Boolean = false
    private var filteredQuery: String = ""

    private val _entryListState = MutableSharedFlow<CustomEntryListState>(replay = 1)
    private val _uiEvent = MutableSharedFlow<CustomEntryUiEvent>()

    val entryListState get() = _entryListState.asSharedFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            launch {
                getCurrentUserUseCase().collectLatest { userId ->
                    _entryListState.emit(CustomEntryListState.InProgress)

                    entries = customEntryUseCases.getCustomEntriesUseCase(userId, 0)

                    if (isFilterDisplayed) {
                        filteredEntries = customEntryUseCases.filterCustomEntriesUseCase(filteredQuery)
                    }

                    _entryListState.emit(CustomEntryListState.UpdateEntries(
                        if (isFilterDisplayed) filteredEntries else entries
                    ))
                }
            }

            launch {
                getCurrentUserUseCase().flatMapLatest {
                    customEntryUseCases.getCustomEntriesCountUseCase(it)
                }.collectLatest { count ->
                    entryCount = count
                }
            }
        }
    }

    fun initializeCustomEntries() {
        viewModelScope.launch {
            resetFilter()
            val userId = getCurrentUserUseCase().first()
            entries = customEntryUseCases.getCustomEntriesUseCase(userId, 0)
        }
    }

    fun loadMoreEntries(scroll: Boolean) {
        viewModelScope.launch {
            val userId = getCurrentUserUseCase().first()
            // TODO: Implement for filtered list
            if (!isFilterDisplayed && !isLoading && entries.size < entryCount) {
                isLoading = true
                entries.add(null)
                _uiEvent.emit(CustomEntryUiEvent.ShowMoreProgress(scroll))

                val moreEntries = customEntryUseCases.getCustomEntriesUseCase(userId, entries.size-1)
                val oldSize = entries.size
                entries.removeLast()
                entries.addAll(moreEntries)

                _uiEvent.emit(CustomEntryUiEvent.AddMoreEntries(oldSize, oldSize + moreEntries.size))
            }

            isLoading = false
        }
    }

    private fun checkEntryLimit() {
        if (entries.size < ENTRY_LIMIT) loadMoreEntries(false)
    }

    private fun resetFilter() {
        viewModelScope.launch {
            filteredQuery = ""
            isFilterDisplayed = false
            filteredEntries = LinkedList()
            _entryListState.emit(CustomEntryListState.UpdateEntries(entries))
            _uiEvent.emit(CustomEntryUiEvent.ResetFilter)
        }
    }

    fun watchFilter(oldQuery: String, newQuery: String) {
        if (oldQuery.isNotEmpty()) {
            viewModelScope.launch {
                if (newQuery.isEmpty()) {
                    resetFilter()
                }
            }
        }
    }

    fun filterEntries(newQuery: String) {
        val newFilter = newQuery.lowercase().trim()
        if (filteredQuery != newFilter) {
            viewModelScope.launch(Dispatchers.Default) {
                _entryListState.emit(CustomEntryListState.InProgress)

                if (newFilter.isEmpty()) {
                    resetFilter()
                } else {
                    isFilterDisplayed = true
                    filteredEntries = customEntryUseCases.filterCustomEntriesUseCase(newFilter)

                    filteredQuery = newFilter
                    _entryListState.emit(CustomEntryListState.UpdateEntries(filteredEntries))
                }
            }
        }
    }

    fun handleResult(result: ActivityResult) {
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val payload: ItemEntryPayload? = result.data!!.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)

            payload?.let {
                val containsFilterQuery = payload.payload.entry.startsWith(filteredQuery)
                val updateFilteredList = containsFilterQuery && isFilterDisplayed

                viewModelScope.launch {
                    if (payload.state == ItemState.ADD) {
                        if (updateFilteredList) filteredEntries.add(0, payload.payload)
                        entries.add(0, payload.payload)
                    } else if (payload.state == ItemState.DELETE) {
                        if (updateFilteredList) filteredEntries.removeAt(filteredPosition)

                        if (realPosition != -1) {
                            entries.removeAt(realPosition)
                        } else {
                            if (!updateFilteredList) payload.state = ItemState.NOTHING
                        }

                        checkEntryLimit()
                    } else if (payload.state == ItemState.UPDATE) {
                        if (updateFilteredList) {
                            filteredEntries.removeAt(filteredPosition)
                            filteredEntries.add(0, payload.payload)
                        }

                        if (realPosition != -1) {
                            entries.removeAt(realPosition)
                            entries.add(0, payload.payload)
                        } else {
                            entries.add(0, payload.payload)
                            if (!updateFilteredList) payload.state = ItemState.ADD
                        }
                    }


                    if (isFilterDisplayed) {
                        if (containsFilterQuery) {
                            _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(filteredPosition, payload.state))
                        }
                    } else {
                        _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(realPosition, payload.state))
                    }
                }

                resetSelections()
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
                if (realPosition != -1) entries.removeAt(realPosition)
            } else {
                entries.removeAt(position)

                checkEntryLimit()
            }

            _uiEvent.emit(CustomEntryUiEvent.UpdateAdapter(position, ItemState.DELETE))
            resetSelections()
        }
    }

    fun createCustomEntry(entry:String) {
        val sanitizedEntry = Formatter.cleanText(entry)
        viewModelScope.launch {
            when(val event = customEntryUseCases.validateCustomEntryUseCase(
                sanitizedEntry,
                if (isFilterDisplayed) filteredEntries else entries
            )) {
                is UserInputState.SameInput<*> -> {
                    if (event.data is Int){
                        if (isFilterDisplayed) filteredPosition = event.data
                        else realPosition = event.data

                        _uiEvent.emit(CustomEntryUiEvent.OpenEntryBuilder(
                            sanitizedEntry,
                            if (isFilterDisplayed) filteredPosition else realPosition
                        ))
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
                        _uiEvent.emit(CustomEntryUiEvent.OpenEntryBuilder(event.data))
                        resetSelections()
                    }
                }
            }
        }
    }

    fun addEntryDataToIntentForBuilder(intent: Intent, sanitizedEntry: String, position: Int): Intent {
        if (position != -1) {
            if (isFilterDisplayed) {
                ensureRealPosition(sanitizedEntry)
                filteredPosition = position
            } else {
                realPosition = position
            }
        }

        intent.putExtra(Constants.ENTRY_KEY, sanitizedEntry)
        return intent
    }

    fun clearUserEntries() {
        viewModelScope.launch {
            customEntryUseCases.removeUserEntriesUseCase()
            entries = LinkedList()
            filteredEntries = LinkedList()
            _entryListState.emit(CustomEntryListState.UpdateEntries(entries))
        }
    }

    private fun resetSelections() {
        realPosition = -1
        filteredPosition = -1
    }

    private fun ensureRealPosition(entry: String) {
        if (realPosition == -1) {
            realPosition = entries.indexOfFirst { it?.entry == entry }
        }
    }
}
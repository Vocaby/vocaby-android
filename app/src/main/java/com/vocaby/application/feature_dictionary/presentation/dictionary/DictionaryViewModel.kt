package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.use_case.DictionaryUseCases
import com.vocaby.application.feature_dictionary_custom.domain.use_case.home.ValidateCustomEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dictionaryUseCases: DictionaryUseCases,
    private val validateCustomEntryUseCase: ValidateCustomEntryUseCase
) : ViewModel() {
    private var entriesByCharacter: List<String> = ArrayList()
    private val suggestionScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var suggestionJob: Job? = null

    private val _uiEvent = MutableSharedFlow<DictionaryHomeUiEvent>()
    private val _searchedEntry = MutableSharedFlow<String>()
    private val _dailyPick = MutableStateFlow<DailyPickState>(DailyPickState.InProgress)
    private val _prevPicks = MutableStateFlow<List<String>>(ArrayList())
    private val _searchSuggestions = MutableSharedFlow<GenericState<List<SearchSuggestionItem>>>()
    private val _searchHistory = MutableSharedFlow<List<SimpleEntryModel>?>(replay=1)
    private var _dictionaryIsReady: Boolean = false

    val searchedEntry get() = _searchedEntry.asSharedFlow().distinctUntilChanged().filter { it.isNotEmpty() }
    val dailyPick get() = _dailyPick.asStateFlow()
    val prevPicks get() = _prevPicks.asStateFlow()
    val searchSuggestions get() = _searchSuggestions.asSharedFlow()
    val searchHistory get() = _searchHistory.asSharedFlow()
    val dictionaryIsReady get() = _dictionaryIsReady
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            dictionaryUseCases.clearDictionaryCacheUseCase()
            _dictionaryIsReady = true
        }

        viewModelScope.launch {
            dictionaryUseCases.getPrevPick().collect { picks ->
                val prevPicks = mutableListOf<String>()
                val (api, random) = picks
                if (api.isNotEmpty()) prevPicks.add(api)
                if (random.isNotEmpty()) prevPicks.add(random)
                if (prevPicks.isNotEmpty()) _prevPicks.emit(prevPicks)
            }
        }

        viewModelScope.launch {
            dictionaryUseCases.getDailyPick().collect {
                _dailyPick.value = it
            }
        }

        getHistory()
        checkNotification()
    }

    private fun checkNotification() {
        val receivedEntry = savedStateHandle.get<String>(MainActivity.NOTIFICATION_SEARCH)

        receivedEntry?.let {
            search(it)
        }
    }

    private fun getHistory() {
        val historyList = dictionaryUseCases.getSearchHistoryUseCase()
        viewModelScope.launch {
            _searchHistory.emit(historyList)
        }
    }

    fun search(entry: String) {
        val sanitized = entry.lowercase().trim()
        viewModelScope.launch {
            _searchedEntry.emit(sanitized)
        }
    }

    // notify observer of history selection
    fun getHistoryDefinition(position: Int) {
        search(dictionaryUseCases.getSearchHistoryItemUseCase(position) ?: "")
    }

    fun createCustomEntry(entry:String) {
        val sanitizedEntry = Formatter.cleanText(entry)

        viewModelScope.launch {
            when(val event = validateCustomEntryUseCase(
                sanitizedEntry
            )) {
                is UserInputState.LongInput -> {
                    _uiEvent.emit(DictionaryHomeUiEvent.ShowAlert("This entry is too long"))
                }
                is UserInputState.EmptyInput -> {
                    _uiEvent.emit(DictionaryHomeUiEvent.ShowAlert("Please enter a word or phrase"))
                }
                is UserInputState.InvalidInput -> {
                    _uiEvent.emit(DictionaryHomeUiEvent.ShowAlert("The entry contains special characters"))
                }
                is UserInputState.Valid<*> -> {
                    if (event.data is String){
                        _uiEvent.emit(DictionaryHomeUiEvent.OpenEntryBuilder(event.data))
                    }
                }
                else -> {}
            }
        }
    }

    fun getSearchSuggestions(newQuery:String) {
        val query = newQuery.lowercase()
        if (newQuery.isEmpty()) {
            viewModelScope.launch {
                resetSearchSuggestion()
                _searchSuggestions.emit(GenericState.Success(ArrayList()))
            }
        } else if (entriesByCharacter.isNullOrEmpty() || !newQuery.startsWith(entriesByCharacter.first().first())) {
            viewModelScope.launch {
                dictionaryUseCases.getDictionaryEntriesByCharacter(query).collectLatest { result ->
                    when(result) {
                        is GenericState.InProgress -> {
                            _searchSuggestions.emit(GenericState.InProgress)
                        }
                        is GenericState.Success -> {
                            entriesByCharacter = result.data
                            setSearchSuggestionItems(query)
                        }
                        is GenericState.Error -> {
                            entriesByCharacter = ArrayList()
                        }
                    }
                }
            }
        } else {
            setSearchSuggestionItems(query)
        }
    }

    private fun setSearchSuggestionItems(searchQuery: String) {
        viewModelScope.launch {
            suggestionJob?.cancelAndJoin()

            suggestionJob = suggestionScope.launch {
                val newSuggestions = dictionaryUseCases.getSearchSuggestionsUseCase(searchQuery, entriesByCharacter)
                _searchSuggestions.emit(GenericState.Success(newSuggestions))
            }
        }
    }

    private fun resetSearchSuggestion() {
        entriesByCharacter = ArrayList()
    }

    fun clearHistory() {
        viewModelScope.launch {
            _searchHistory.emit(dictionaryUseCases.eraseSearchHistoryUserCase())
        }
    }

    fun writeToHistory(entry: String, dictionaryResult: DictionarySearchResult) {
        viewModelScope.launch {
            _searchHistory.emit(dictionaryUseCases.insertSearchHistoryUseCase(entry, dictionaryResult))
        }
    }

    fun resetSearch() {
        viewModelScope.launch {
            _searchedEntry.emit("")
        }
    }

    fun handleResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            resetSearchSuggestion()
        }
    }

    override fun onCleared() {
        suggestionScope.cancel()
        super.onCleared()
    }
}
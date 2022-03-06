package com.vocaby.application.feature_dictionary.presentation.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.use_case.DictionaryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val dictionaryUseCases: DictionaryUseCases
) : ViewModel() {
    private var entriesByCharacter: List<String> = ArrayList()
    private val suggestionScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var suggestionJob: Job? = null

    private val _searchedEntry = MutableSharedFlow<String>()
    private val _dailyPick = MutableStateFlow<DailyPickState>(DailyPickState.InProgress)
    private val _searchSuggestions = MutableSharedFlow<GenericState<List<SearchSuggestionItem>>>()
    private val _searchHistory = MutableSharedFlow<List<SimpleEntryModel>?>(replay=1)
    private var _dictionaryIsReady: Boolean = false

    val searchedEntry get() = _searchedEntry.asSharedFlow().distinctUntilChanged().filter { it.isNotEmpty() }
    val dailyPick get() = _dailyPick.asSharedFlow()
    val searchSuggestions get() = _searchSuggestions.asSharedFlow()
    val searchHistory get() = _searchHistory.asSharedFlow()
    val dictionaryIsReady get() = _dictionaryIsReady

    init {
        viewModelScope.launch {
            dictionaryUseCases.clearDictionaryCacheUseCase()
            _dictionaryIsReady = true
        }

        getHistory()
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

    fun resetSearchSuggestion() {
        entriesByCharacter = ArrayList()
    }

    fun updateDailyPick() {
        viewModelScope.launch {
            _dailyPick.value = dictionaryUseCases.getDailyPick()
        }
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

    override fun onCleared() {
        suggestionScope.cancel()
        super.onCleared()
    }
}
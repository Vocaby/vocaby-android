package com.vocaby.application.feature_dictionary.presentation.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
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
    private val searchStack: ArrayDeque<String> = ArrayDeque()
    private val suggestionScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _searchedEntry = MutableSharedFlow<String>()
    private val _dailyPick = MutableSharedFlow<DailyPickState>()
    private val _searchSuggestions = MutableSharedFlow<GenericState<List<SearchSuggestionItem>>>()
    private val _searchHistory = MutableSharedFlow<List<SimpleEntryModel>?>(replay=1)

    val searchedEntry get() = _searchedEntry.asSharedFlow()
    val dailyPick get() = _dailyPick.asSharedFlow()
    val searchSuggestions get() = _searchSuggestions.asSharedFlow()
    val searchHistory get() = _searchHistory.asSharedFlow()

    init {
        getHistory()
    }

    private fun getHistory() {
        val historyList = dictionaryUseCases.getSearchHistoryUseCase()
        viewModelScope.launch {
            _searchHistory.emit(historyList)
        }
    }

    // validate and notify observer of new search
    fun search(entry: String) {
        val validatedSearch = dictionaryUseCases.validateSearchUserCase(entry, searchStack)
        viewModelScope.launch {
            validatedSearch?.let { _searchedEntry.emit(it) }
        }
    }

    // notify observer of history selection
    fun getHistoryDefinition(position: Int) {
        search(dictionaryUseCases.getSearchHistoryItemUseCase(position) ?: "")
    }

    fun getSearchSuggestions(newQuery:String) {
        if (newQuery.isEmpty() || entriesByCharacter.isNullOrEmpty()) {
            viewModelScope.launch {
                dictionaryUseCases.getDictionaryEntriesByCharacter(newQuery).collectLatest { result ->
                    when(result) {
                        is GenericState.InProgress -> {
                            _searchSuggestions.emit(GenericState.InProgress)
                        }
                        is GenericState.Success -> {
                            entriesByCharacter = result.data
                            setSearchSuggestionItems(newQuery)
                        }
                        is GenericState.Error -> {
                            entriesByCharacter = ArrayList()
                        }
                    }
                }
            }
        } else {
            setSearchSuggestionItems(newQuery)
        }
    }

    private fun setSearchSuggestionItems(searchQuery: String) {
        dictionaryUseCases.getSearchSuggestionsUseCase(searchQuery, entriesByCharacter).onEach { searchSuggestions ->
            _searchSuggestions.emit(GenericState.Success(searchSuggestions))
        }.launchIn(suggestionScope)
    }

    fun resetSearchSuggestion() {
        entriesByCharacter = ArrayList()
    }

    // return to observer of random word
    fun updateDailyPick() {
        viewModelScope.launch {
            dictionaryUseCases.getDailyPick().collectLatest { _dailyPick.emit(it) }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _searchHistory.emit(dictionaryUseCases.eraseSearchHistoryUserCase())
        }
    }

    fun writeToHistory(entry: String, entryList: List<EntryModel?>) {
        viewModelScope.launch {
            _searchHistory.emit(dictionaryUseCases.insertSearchHistoryUseCase(entry, entryList))
        }
    }

    fun popSearchStack() {
        if (searchStack.isNotEmpty()) searchStack.removeLast()
    }

    fun clearCache() {
        dictionaryUseCases.clearDictionaryCacheUseCase()
    }

    override fun onCleared() {
        suggestionScope.cancel()
        super.onCleared()
    }
}
package com.vocaby.application.feature_dictionary.presentation.dictionary

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.use_case.DictionaryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val dictionaryUseCases: DictionaryUseCases
) : ViewModel() {
    private var entriesByCharacter: List<String> = ArrayList()
    private val searchStack: ArrayDeque<String> = ArrayDeque()
    private val suggestionScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _searchedEntry: SingleLiveEvent<String> = SingleLiveEvent()
    private val _dailyPick: SingleLiveEvent<DailyPick> = SingleLiveEvent()
    private val _searchHistory: SingleLiveEvent<List<SimpleEntryModel>?> = SingleLiveEvent()
    private val _searchSuggestions: SingleLiveEvent<GenericState<List<SearchSuggestionItem>>> = SingleLiveEvent()

    val searchedEntry: LiveData<String> get() = _searchedEntry
    val searchHistory: LiveData<List<SimpleEntryModel>?> get() = _searchHistory
    val dailyPick: LiveData<DailyPick> get() = _dailyPick
    val searchSuggestions: LiveData<GenericState<List<SearchSuggestionItem>>> get() = _searchSuggestions

    fun getHistory() {
        val historyList = dictionaryUseCases.getSearchHistoryUseCase()
        historyList?.let {
            _searchHistory.value = historyList
        }
    }

    // validate and notify observer of new search
    fun search(entry: String) {
        val validatedSearch = dictionaryUseCases.validateSearchUserCase(entry, searchStack)
        validatedSearch?.let { _searchedEntry.value = it }
    }

    // notify observer of history selection
    fun getHistoryDefinition(position: Int) {
        _searchHistory.value?.let { list ->
            search(list[position].entry)
        }
    }

    fun getSearchSuggestions(newQuery:String) {
        if (newQuery.isEmpty() || entriesByCharacter.isNullOrEmpty()) {
            dictionaryUseCases.getDictionaryEntriesByCharacter(newQuery).onEach { result ->
                when(result) {
                    is GenericState.InProgress -> {
                        _searchSuggestions.postValue(GenericState.InProgress)
                    }
                    is GenericState.Success -> {
                        entriesByCharacter = result.data
                        setSearchSuggestionItems(newQuery)
                    }
                    is GenericState.Error -> {
                        entriesByCharacter = ArrayList()
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            setSearchSuggestionItems(newQuery)
        }
    }

    private fun setSearchSuggestionItems(searchQuery: String) {
        dictionaryUseCases.getSearchSuggestionsUseCase(searchQuery, entriesByCharacter).onEach { searchSuggestions ->
            _searchSuggestions.postValue(GenericState.Success(searchSuggestions))
        }.launchIn(suggestionScope)
    }

    fun resetSearchSuggestion() {
        entriesByCharacter = ArrayList()
    }

    // return to observer of random word
    fun updateDailyPick() {
        viewModelScope.launch {
            _dailyPick.postValue(dictionaryUseCases.getDailyPick())
        }
    }

    fun clearHistory() {
        _searchHistory.value = dictionaryUseCases.eraseSearchHistoryUserCase()
    }

    fun writeToHistory(entry: String, entryList: List<EntryModel?>) {
        _searchHistory.value = dictionaryUseCases.insertSearchHistoryUseCase(entry, entryList)
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
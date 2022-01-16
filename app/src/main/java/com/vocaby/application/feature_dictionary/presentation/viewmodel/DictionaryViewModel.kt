package com.vocaby.application.feature_dictionary.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.Formatter.cleanText
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.core.util.VocabyAlgo
import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.states.GenericState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {
    var searchSuggestionThreshold: Int = 4

    private val _searchedEntry: SingleLiveEvent<String> = SingleLiveEvent()
    private val _dailyPick: SingleLiveEvent<DailyPick> = SingleLiveEvent()
    private val _searchHistory: SingleLiveEvent<List<SimpleEntryModel>?> = SingleLiveEvent()
    private val _searchSuggestions: SingleLiveEvent<GenericState<List<SearchSuggestionItem>>> = SingleLiveEvent()
    private val searchStack: ArrayDeque<String> = ArrayDeque()
    private var entriesByCharacter: List<String>? = null

    val searchedEntry: LiveData<String> get() = _searchedEntry
    val searchHistory: LiveData<List<SimpleEntryModel>?> get() = _searchHistory
    val dailyPick: LiveData<DailyPick> get() = _dailyPick
    val searchSuggestions: LiveData<GenericState<List<SearchSuggestionItem>>> get() = _searchSuggestions

    fun getHistory() {
        val historyList = userRepository.getHistory()
        historyList?.let {
            _searchHistory.value = historyList
        }
    }

    // notify observer of new search
    fun search(entry: String) {
        val searchedEntry = cleanText(entry)
        if (searchedEntry.isNotEmpty()) {
            if (searchStack.isNotEmpty()) {
                if (!searchStack.contains(searchedEntry)) {
                    searchStack.removeLast()
                    searchStack.addLast(searchedEntry)
                    _searchedEntry.value = searchedEntry
                }
            } else {
                searchStack.addLast(searchedEntry)
                _searchedEntry.value = searchedEntry
            }
        }
    }

    // notify observer of history selection
    fun getHistoryDefinition(position: Int) {
        _searchHistory.value?.let { list ->
            search(list[position].entry)
        }
    }

    fun getSearchSuggestions(newQuery:String) {
        val query = newQuery.lowercase()
        if (query.isEmpty()) {
            _searchSuggestions.postValue(GenericState.Success(ArrayList()))
            entriesByCharacter = null
        } else if (entriesByCharacter == null && query.isNotEmpty()) {
            _searchSuggestions.postValue(GenericState.InProgress)
            val initialCharacter = query.substring(0, 1)
            viewModelScope.launch(Dispatchers.Default) {
                entriesByCharacter = dictionaryRepository.getEntriesByCharacterFromDB(initialCharacter)
                setSearchSuggestionItems(query)
            }
        } else {
            _searchSuggestions.postValue(GenericState.InProgress)
            setSearchSuggestionItems(query)
        }
    }

    private fun setSearchSuggestionItems(searchQuery: String) {
        val searchSuggestionItems = ArrayList<SearchSuggestionItem>()
        if (!entriesByCharacter.isNullOrEmpty()) {
            var index = VocabyAlgo.binarySearchPrefix(entriesByCharacter!!, searchQuery)
            if (index > -1 && index < entriesByCharacter!!.size) {
                val it: Iterator<String> = entriesByCharacter!!.listIterator(index)
                var count = 0
                while (it.hasNext() && count < searchSuggestionThreshold) {
                    val entry = it.next()
                    if (entry.contains(searchQuery)) {
                        searchSuggestionItems.add(SearchSuggestionItem(entry))
                    }

                    count++
                    index++
                }
            }
        }

        _searchSuggestions.postValue(GenericState.Success(searchSuggestionItems))
    }

    fun resetSearchSuggestion() {
        entriesByCharacter = null
    }

    // return to observer of random word
    fun updateDailyPick() {
        viewModelScope.launch {
            val pick: DailyPick = dictionaryRepository.getDailyPick()
            _dailyPick.postValue(pick)
        }
    }

    fun clearHistory() {
        _searchHistory.value = userRepository.clearHistory()
    }

    fun writeToHistory(entry: String, entryList: List<EntryModel?>) {

        val historyList: List<SimpleEntryModel>? = if (entryList.isEmpty()) {
            userRepository.writeToHistory(SimpleEntryModel(entry, ""))
        } else {
            val definition = entryList[0]?.firstGroup?.let {
                it.definitionData[0].definition
            } ?: "No definition was found"

            userRepository.writeToHistory(SimpleEntryModel(entry, definition))
        }


        _searchHistory.value = historyList
    }

    fun popSearchStack() {
        if (searchStack.isNotEmpty()) searchStack.removeLast()
    }

    fun clearCache() {
        dictionaryRepository.clearDictionaryCache()
    }
}
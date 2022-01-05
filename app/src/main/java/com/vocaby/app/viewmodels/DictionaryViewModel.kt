package com.vocaby.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.models.SearchSuggestionItem
import com.vocaby.app.models.dictionary.DailyPick
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.models.dictionary.SimpleEntryModel
import com.vocaby.app.states.GenericState
import com.vocaby.app.utils.SingleLiveEvent
import com.vocaby.app.utils.StringFormatter.cleanText
import com.vocaby.app.utils.VocabyAlgo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DictionaryViewModel(private val repository: VocabyRepository) : ViewModel() {
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
        val historyList = repository.getHistory()
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
                entriesByCharacter = repository.getEntriesByCharacterFromDB(initialCharacter)
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
            val pick: DailyPick = repository.getDailyPick()
            _dailyPick.postValue(pick)
        }
    }

    fun clearHistory() {
        _searchHistory.value = repository.clearHistory()
    }

    fun writeToHistory(entry: String, entryList: List<EntryModel?>) {
        val historyList: List<SimpleEntryModel>?

        if (entryList.isEmpty()) {
            historyList = repository.writeToHistory(SimpleEntryModel(entry, ""))
        } else {
            val definition = entryList[0]?.firstGroup?.let {
                it.definitionData[0].definition
            } ?: "No definition was found"

            historyList = repository.writeToHistory(SimpleEntryModel(entry, definition))
        }


        _searchHistory.value = historyList
    }

    fun popSearchStack() {
        if (searchStack.isNotEmpty()) searchStack.removeLast()
    }

    fun clearCache() {
        repository.clearDictionaryCache()
    }
}

class DictionaryViewModelFactory(
    private val repository: VocabyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
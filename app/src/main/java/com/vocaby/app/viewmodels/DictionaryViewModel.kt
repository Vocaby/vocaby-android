package com.vocaby.app.viewmodels

import androidx.lifecycle.*
import com.vocaby.app.data.VocabyRepositoryKt
import com.vocaby.app.models.SearchSuggestionItem
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.states.GenericState
import com.vocaby.app.utils.SingleLiveEvent
import com.vocaby.app.utils.StringFormatter.cleanText
import com.vocaby.app.utils.VocabyAlgo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DictionaryViewModel(private val repository: VocabyRepositoryKt) : ViewModel() {
    var searchSuggestionThreshold: Int = 4

    private val _searchedEntry: MutableLiveData<String> = MutableLiveData()
    private val _searchHistory: MutableLiveData<List<String>> = MutableLiveData()
    private val _randomEntry: MutableLiveData<EntryModel> = MutableLiveData()
    private val _searchSuggestions: SingleLiveEvent<GenericState<List<SearchSuggestionItem>>> = SingleLiveEvent()
    private val searchStack: ArrayDeque<String> = ArrayDeque()
    private var entriesByCharacter: List<String>? = null

    val searchedEntry: LiveData<String> get() = _searchedEntry
    val searchHistory: LiveData<List<String>> get() = _searchHistory
    val randomEntry: LiveData<EntryModel> get() = _randomEntry
    val searchSuggestions: LiveData<GenericState<List<SearchSuggestionItem>>> get() = _searchSuggestions

    init {
        val historyList = repository.getHistory()
        _searchHistory.postValue(historyList)
    }

    // create shared prefs based on first character
    fun setupDictionaryEntries() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.setupDictionaryEntries()
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
                    writeToHistory(searchedEntry)
                    _searchedEntry.postValue(searchedEntry)
                }
            } else {
                searchStack.addLast(searchedEntry)
                writeToHistory(searchedEntry)
                _searchedEntry.postValue(searchedEntry)
            }
        }
    }

    // notify observer of history selection
    fun getHistoryDefinition(position: Int) {
        _searchHistory.value?.let { list ->
            search(list[position])
        }
    }

    fun getSearchSuggestions(oldQuery: String, newQuery:String) {
        if (newQuery.isEmpty()) {
            _searchSuggestions.postValue(GenericState.Success(ArrayList()))
            entriesByCharacter = null
        } else if (oldQuery.isEmpty() && newQuery.length == 1) {
            _searchSuggestions.postValue(GenericState.InProgress)
            val initialCharacter = newQuery.substring(0, 1)
            viewModelScope.launch(Dispatchers.Default) {
                entriesByCharacter = repository.getEntriesByCharacter(initialCharacter)
                setSearchSuggestionItems(newQuery)
            }
        } else {
            _searchSuggestions.postValue(GenericState.InProgress)
            setSearchSuggestionItems(newQuery)
        }
    }
    private fun setSearchSuggestionItems(searchQuery: String) {
        val searchSuggestionItems = ArrayList<SearchSuggestionItem>()
        if (entriesByCharacter.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.Default) {
                val initialCharacter = searchQuery.substring(0, 1)
                entriesByCharacter = repository.getEntriesByCharacter(initialCharacter)
                setSearchSuggestionItems(searchQuery)
            }
        } else {
            var index = VocabyAlgo.BinarySearchPrefix(entriesByCharacter, searchQuery)
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

            _searchSuggestions.postValue(GenericState.Success(searchSuggestionItems))
        }
    }

    fun resetSearchSuggestion() {
        entriesByCharacter = null
    }

    // return to observer of random word
    fun updateRandomWord() {
        viewModelScope.launch {
            val data: EntryModel? = repository.getRandomEntry()
            data?.let {
                _randomEntry.postValue(data)
            }
        }
    }

    fun clearHistory() {
        _searchHistory.value = repository.clearHistory()
    }

    private fun writeToHistory(entry: String) {
        val historyList = repository.writeToHistory(entry)
        _searchHistory.postValue(historyList)
    }

    fun popSearchStack() {
        if (searchStack.isNotEmpty()) searchStack.removeLast()
    }
}

class DictionaryViewModelFactory(
    private val repository: VocabyRepositoryKt
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.vocaby.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vocaby.app.data.VocabyRepositoryKt
import com.vocaby.app.models.SearchSuggestionItem
import com.vocaby.app.models.dictionary.EntryModel

class DictionaryViewModelKt(private val repository: VocabyRepositoryKt) : ViewModel() {
    private val _searchedEntry: MutableLiveData<String> = MutableLiveData()
    private val _searchHistory: MutableLiveData<List<String>> = MutableLiveData()
    private val _randomEntry: MutableLiveData<EntryModel> = MutableLiveData()
    private val _searchSuggestions: MutableLiveData<List<SearchSuggestionItem>> = MutableLiveData()
    private val searchStack: ArrayDeque<String> = ArrayDeque()

    val searchedEntry: MutableLiveData<String> get() = _searchedEntry
    val searchHistory: MutableLiveData<List<String>> get() = _searchHistory
    val randomEntry: MutableLiveData<EntryModel> get() = _randomEntry
    val searchSuggestions: MutableLiveData<List<SearchSuggestionItem>> get() = _searchSuggestions

    // create shared prefs based on first character
    fun setupDictionaryEntries() {}

    // notify observer of new search
    fun search(entry: String) {}

    // notify observer of history selection
    fun getHistoryDefinition(position: Int) {}

    fun getSearchSuggestions(oldQuery: String, newQuery:String) {}
    private fun setSearchSuggestionItems(threshold: Int) {}

    // return to observer of random word
    fun updateRandomWord() {}

    //
    fun resetDictionaryEntries() {}

}

class DictionaryViewModelFactory(
    private val repository: VocabyRepositoryKt
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModelKt::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModelKt(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
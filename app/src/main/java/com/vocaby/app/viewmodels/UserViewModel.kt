package com.vocaby.app.viewmodels

import androidx.lifecycle.*
import com.vocaby.app.data.VocabyRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModel(private val repository: VocabyRepository) : ViewModel() {
    private val _savedWords: MutableLiveData<List<String>> = MutableLiveData(ArrayList())
    private val _savesCount: MutableLiveData<Int> = MutableLiveData(0)

    val savedWords: LiveData<List<String>> get() = _savedWords
    val savesCount: LiveData<Int> get() = _savesCount

    fun setupUser() = viewModelScope.launch {
        repository.setupUser()

        repository.getSavedWordsFlow().collect { saves ->
            _savedWords.postValue(saves)
            _savesCount.postValue(saves.size)
        }
    }

    fun addSaveItem(entry: String) = viewModelScope.launch {
        repository.addSaveItem(entry)
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        repository.removeSaveItem(entry)
    }

    fun clearSaves() = viewModelScope.launch {
        repository.clearSaves()
    }
}

class UserViewModelFactory(private val repository: VocabyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
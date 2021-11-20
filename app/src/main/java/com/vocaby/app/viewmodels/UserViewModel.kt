package com.vocaby.app.viewmodels

import androidx.lifecycle.*
import com.vocaby.app.data.VocabyRepositoryKt
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModelKt(private val repository: VocabyRepositoryKt) : ViewModel() {
    private val _savedWords: MutableLiveData<List<String>> = MutableLiveData(ArrayList())
    private val _savesCount: MutableLiveData<Int> = MutableLiveData(0)

    val savedWords: LiveData<List<String>> get() = _savedWords
    val savesCount: LiveData<Int> get() = _savesCount

    init {
        viewModelScope.launch {
            repository.getSavedWords().collect { saves ->
                _savedWords.postValue(saves)
                _savesCount.postValue(saves.size)
            }
        }
    }

    fun setupUser() = viewModelScope.launch {
        repository.setupUser()
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

class UserViewModelFactory(private val repository: VocabyRepositoryKt) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModelKt::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModelKt(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
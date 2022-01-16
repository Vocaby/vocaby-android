package com.vocaby.application.feature_save.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    var userId: Int = 1
    private val _savedWords: MutableLiveData<List<String>> = MutableLiveData(ArrayList())
    private val _savesCount: MutableLiveData<Int> = MutableLiveData(0)

    init {
        viewModelScope.launch {
            userId = userRepository.getUser()
            userRepository.getSavedWordsFlow(userId).collect { saves ->
                _savedWords.postValue(saves)
                _savesCount.postValue(saves.size)
            }
        }
    }

    val savedWords: LiveData<List<String>> get() = _savedWords
    val savesCount: LiveData<Int> get() = _savesCount

    fun addSaveItem(entry: String) = viewModelScope.launch {
        userRepository.addSaveItem(userId, entry)
    }

    fun removeSaveItem(entry: String) = viewModelScope.launch {
        userRepository.removeSaveItem(userId, entry)
    }

    fun clearSaves() = viewModelScope.launch {
        userRepository.clearSaves(userId)
    }

}
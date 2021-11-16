package com.vocaby.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.app.data.VocabyRepositoryKt
import kotlinx.coroutines.launch

class UserViewModelKt(private val repository: VocabyRepositoryKt) : ViewModel() {

    fun setupUser() = viewModelScope.launch {
        repository.setupUser()
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
package com.vocaby.application.feature_user.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    var userId: Int = 1
    fun setupUser() {
        viewModelScope.launch {
            repository.setupUser(userId)
        }
    }

    fun isUseConnectionEnabled() = repository.isUseConnectionEnabled()
    fun setConnectionSettings(enabled: Boolean) = repository.setConnectionSettings(enabled)

    fun isDataShareEnabled() = repository.isDataShareEnabled()
    fun setDataShareSettings(enabled: Boolean) = repository.setDataShareSettings(enabled)
}
package com.vocaby.application.feature_profile.presentation.setting

import androidx.lifecycle.ViewModel
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel() {
    fun isUseConnectionEnabled() = repository.isUseConnectionEnabled()
    fun setConnectionSettings(enabled: Boolean) = repository.setConnectionSettings(enabled)

    fun isDataShareEnabled() = repository.isDataShareEnabled()
    fun setDataShareSettings(enabled: Boolean) = repository.setDataShareSettings(enabled)
}
package com.vocaby.application.feature_profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.app.UserSettings
import com.vocaby.application.feature_profile.domain.model.ProfileModel
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_profile.domain.use_case.ProfileUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCases: ProfileUseCases,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
): ViewModel() {
    private var _userIsReady: Boolean = false
    private var _cleanedUp: Boolean = false
    private val _chartState = MutableSharedFlow<ChartState>(replay = 1)
    private val _chartModeAll = MutableStateFlow(false)
    private val _profileState = MutableStateFlow<ProfileModel?>(null)

    val chartState get() = _chartState.asSharedFlow()
    val chartModeAll get() = _chartModeAll.asStateFlow()
    val profileState get() = _profileState.asStateFlow()
    val isReady get() = _userIsReady && _cleanedUp

    init {
        viewModelScope.launch {
            launch {
                _userIsReady = profileUseCases.setupBaseUserUseCase()
                profileUseCases.cleanUpUserUseCase()
                _cleanedUp = true
            }

            launch {
                getCurrentUserUseCase().flatMapLatest {
                    profileUseCases.getProfileDataUseCase(it)
                }.collectLatest { data ->
                    _profileState.emit(data)
                }
            }

            launch {
                profileUseCases.getChartSettingsUseCase().collectLatest { chartMode ->
                    when(chartMode) {
                        UserSettings.ChartMode.ALL -> {
                            _chartModeAll.value = true
                        }
                        UserSettings.ChartMode.MONTHLY -> {
                            _chartModeAll.value = false
                        }
                        UserSettings.ChartMode.UNRECOGNIZED -> {
                            _chartModeAll.value = true
                        }
                    }
                }
            }
        }
    }

    fun updateChart() {
        val mode = if (_chartModeAll.value) {
            UserSettings.ChartMode.ALL
        } else {
            UserSettings.ChartMode.MONTHLY
        }

        viewModelScope.launch(Dispatchers.Default) {
            profileUseCases.updateChartUseCase(mode).collectLatest { chartState ->
                _chartState.emit(chartState)
            }
        }
    }

    fun eraseChartData() {
        viewModelScope.launch(Dispatchers.IO) {
            profileUseCases.eraseChartDataUseCase()
        }
    }

    fun reset() {
        viewModelScope.launch {
            profileUseCases.resetUseCase()
            profileUseCases.eraseChartDataUseCase()
        }
    }
}
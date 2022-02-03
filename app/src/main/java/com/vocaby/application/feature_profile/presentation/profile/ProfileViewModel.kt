package com.vocaby.application.feature_profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.app.UserSettings
import com.vocaby.application.core.util.Logger
import com.vocaby.application.core.util.Logger.reportToDebug
import com.vocaby.application.feature_profile.domain.use_case.ProfileUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCases: ProfileUseCases
): ViewModel() {
    private var _userIsReady: Boolean = false
    private var _cleanedUp: Boolean = false
    private val _chartState = MutableSharedFlow<ChartState>(replay = 1)
    private val _chartModeAll = MutableStateFlow(false)

    val chartState get() = _chartState.asSharedFlow()
    val chartModeAll get() = _chartModeAll.asStateFlow()
    val isReady get() = _userIsReady && _cleanedUp

    init {
        viewModelScope.launch {
            _userIsReady = profileUseCases.setupBaseUserUseCase()
            Logger.reportToDebug("user set up")
            profileUseCases.cleanUpUserUseCase()
            Logger.reportToDebug("cleaned up")
            _cleanedUp = true
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

    fun initializeChart() {
        viewModelScope.launch {
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

    fun eraseChartData() {
        viewModelScope.launch(Dispatchers.IO) {
            profileUseCases.eraseChartDataUseCase()
        }
    }
}
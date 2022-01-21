package com.vocaby.application.feature_user.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_user.domain.use_case.ProfileUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCases: ProfileUseCases
): ViewModel() {
    private val _chartState = MutableSharedFlow<ChartState>(replay = 1)
    private val _chartMode = MutableStateFlow(false)

    val chartState get() = _chartState.asSharedFlow()
    val chartMode get() = _chartMode.asStateFlow()


    fun setupUser() {
        viewModelScope.launch {
            profileUseCases.setupUserUseCase()
        }
    }

    fun updateChart() {
        viewModelScope.launch(Dispatchers.Default) {
            profileUseCases.updateChartUseCase().collectLatest { chartState ->
                _chartState.emit(chartState)
            }
        }
    }

    fun changeChartMode(displayAll: Boolean) {
        profileUseCases.updateChartModeUseCase(displayAll)
        _chartMode.value = displayAll
    }

    fun initializeChart() {
        _chartMode.value = profileUseCases.getChartModeUseCase()
    }

    fun eraseChartData() {
        viewModelScope.launch(Dispatchers.IO) {
            profileUseCases.eraseChartDataUseCase()
        }
    }
}
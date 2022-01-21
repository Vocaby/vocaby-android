package com.vocaby.application.feature_user.presentation.profile

import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_user.domain.model.ChartData

sealed class ChartState {
    data class Success(val chartData: ChartData, val colors: List<Int>, val favourite: String): ChartState()
    data class Error(val exception: Exception): ChartState()
    object InProgress: ChartState()
}

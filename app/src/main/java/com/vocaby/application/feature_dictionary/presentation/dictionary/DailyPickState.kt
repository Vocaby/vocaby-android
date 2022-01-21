package com.vocaby.application.feature_dictionary.presentation.dictionary

import com.vocaby.application.feature_dictionary.domain.model.DailyPick

sealed class DailyPickState {
    data class Picked(val pick: DailyPick): DailyPickState()
    object InProgress: DailyPickState()
}

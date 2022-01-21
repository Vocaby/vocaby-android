package com.vocaby.application.feature_save.presentation.save

sealed class SaveState {
    data class Fetched(val saved: Boolean): SaveState()
    object InProgress: SaveState()
    object Remove: SaveState()
}
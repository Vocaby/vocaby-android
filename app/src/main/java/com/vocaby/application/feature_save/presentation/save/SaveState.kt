package com.vocaby.application.feature_save.presentation.save

sealed class SaveState {
    data class Processed(val saved: Boolean): SaveState()
    object InProgress: SaveState()
    object Remove: SaveState()
    object Show: SaveState()
}
package com.vocaby.application.states

sealed class SaveState {
    data class Fetched(val saved: Boolean): SaveState()
    object InProgress: SaveState()
    object Remove: SaveState()
}
package com.vocaby.app.states

sealed class SaveState {
    data class Fetched(val saved: Boolean): SaveState()
    object InProgress: SaveState()
    object Remove: SaveState()
}
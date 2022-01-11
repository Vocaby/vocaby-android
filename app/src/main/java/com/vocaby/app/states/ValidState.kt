package com.vocaby.app.states

sealed class ValidState {
    object Valid: ValidState()
    data class Error(val data: String=""): ValidState()
}
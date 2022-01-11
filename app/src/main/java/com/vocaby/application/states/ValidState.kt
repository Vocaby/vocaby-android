package com.vocaby.application.states

sealed class ValidState {
    object Valid: ValidState()
    data class Error(val data: String=""): ValidState()
}
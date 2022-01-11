package com.vocaby.application.states

sealed class UserInputState {
    data class Valid(val data: String): UserInputState()
    object EmptyInput: UserInputState()
    object LongInput: UserInputState()
    object NoInput: UserInputState()
    object InvalidInput: UserInputState()
    object SameInput: UserInputState()
}
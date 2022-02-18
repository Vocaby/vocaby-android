package com.vocaby.application.core.states

sealed class UserInputState {
    data class Valid<out T: Any>(val data: T): UserInputState()
    data class SameInput<out T: Any>(val data: T): UserInputState()
    object EmptyInput: UserInputState()
    object LongInput: UserInputState()
    object NoInput: UserInputState()
    object InvalidInput: UserInputState()
}
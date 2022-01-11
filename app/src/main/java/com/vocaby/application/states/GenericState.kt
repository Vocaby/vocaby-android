package com.vocaby.application.states

sealed class GenericState<out T: Any> {
    data class Success<out T: Any>(val data: T): GenericState<T>()
    data class Error(val exception: Exception): GenericState<Nothing>()
    object InProgress: GenericState<Nothing>()
}
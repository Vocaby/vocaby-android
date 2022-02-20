package com.vocaby.application.core.util

sealed class ResourceState<out T: Any> {
    data class Success<out T: Any>(val data: T? = null): ResourceState<T>()
    data class Error<out T: Any>(val uiText: UiText): ResourceState<T>()
    data class InProgress(val uiText: UiText? = null): ResourceState<Nothing>()
}
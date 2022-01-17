package com.vocaby.application.core.util

sealed class ResourceState<T>(val data: T? = null, val uiText: UiText? = null) {
    class Success<T>(data: T?): ResourceState<T>(data)
    class Error<T>(uiText: UiText, data: T? = null): ResourceState<T>(data, uiText)
    object InProgress: ResourceState<Nothing>()
}
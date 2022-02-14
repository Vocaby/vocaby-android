package com.vocaby.application.feature_datatransfer.presentation

import com.vocaby.application.core.util.UiText

sealed class DataTransferState {
    data class Success(val message: UiText): DataTransferState()
    data class InProgress(val message: UiText, val countMessage: String? = null): DataTransferState()
    data class Error(val uiText: UiText): DataTransferState()
}
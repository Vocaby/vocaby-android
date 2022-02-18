package com.vocaby.application.feature_support.domain.use_case

import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.core.util.UiText
import com.vocaby.application.core.util.Validator
import com.vocaby.application.feature_support.domain.model.FeedbackModel
import com.vocaby.application.feature_support.domain.repository.SupportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SubmitFeedbackUseCase(
    private val supportRepository: SupportRepository
) {
    suspend operator fun invoke (
        selected: String,
        message: String,
        email: String
    ): Flow<ResourceState<Nothing>> = flow {
        val sanitizedMessage = Formatter.cleanText(message)
        if (selected.isEmpty()) {
            emit(ResourceState.Error(UiText(textResource =  R.string.feedback_input_alert_type)))
        } else if (sanitizedMessage.isEmpty()) {
            emit(ResourceState.Error(UiText(textResource =  R.string.feedback_input_alert_message)))
        } else {
            if (email.isNotEmpty() && !Validator.emailIsValid(email)) {
                emit(ResourceState.Error(UiText(textResource = R.string.feedback_input_alert_email)))
            } else {
                emit(ResourceState.InProgress)
                val type: String = when (selected) {
                    "Error Report" -> "ER"
                    "Dictionary Update" -> "DI"
                    "Feature Request" -> "FE"
                    else -> ""
                }

                when (val state =
                    supportRepository.submitFeedback(FeedbackModel(type, message, email))) {
                    is ResourceState.Success -> {
                        emit(ResourceState.Success())
                    }
                    is ResourceState.Error -> {
                        emit(ResourceState.Error(UiText(text = state.uiText.text)))
                    }
                    else -> {}
                }
            }
        }
    }
}
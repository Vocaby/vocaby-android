package com.vocaby.application.feature_support.domain.repository

import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.feature_support.domain.model.FaqModel
import com.vocaby.application.feature_support.domain.model.FeedbackModel

interface SupportRepository {
    suspend fun submitFeedback(feedbackModel: FeedbackModel): ResourceState<Nothing>
    fun getFaq(): List<FaqModel>
}
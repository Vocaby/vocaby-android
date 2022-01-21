package com.vocaby.application.feature_support.data.remote

import com.vocaby.application.feature_support.domain.model.FeedbackModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupportApi {
    @POST("feedback/")
    suspend fun submitFeedback(@Header("Content-Type") contentType: String, @Body feedback: FeedbackModel): Response<Void>
}
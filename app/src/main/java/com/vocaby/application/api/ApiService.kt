package com.vocaby.application.api

import com.vocaby.application.models.FeedbackModel
import com.vocaby.application.models.dictionary.EntryModel
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("dictionary/{entry}")
    suspend fun getDefinitions(@Path("entry") entry: String): Response<EntryModel>

    @GET("dictionary/update-check/{entry}")
    suspend fun checkEntryUpdate(@Path("entry") entry: String): Response<String>

    @GET("wod/get/{date}/")
    suspend fun getWoD(@Path("date") date: String): Response<EntryModel>

    @POST("feedback/")
    suspend fun submitFeedback(@Header("Content-Type") contentType: String, @Body feedback: FeedbackModel): Response<Void>
}
package com.vocaby.application.api

import com.vocaby.application.models.FeedbackModel
import com.vocaby.application.models.dictionary.EntryModel
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("dictionary/get/{entry}/{date}/")
    suspend fun checkAndGetDefinitions(@Path("entry") entry: String, @Path("date") date: String): Response<EntryModel>

    @GET("wod/get/{date}/")
    suspend fun getWoD(@Path("date") date: String): Response<EntryModel>

    @POST("feedback/")
    suspend fun submitFeedback(@Header("Content-Type") contentType: String, @Body feedback: FeedbackModel): Response<Void>
}
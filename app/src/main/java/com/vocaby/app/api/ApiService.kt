package com.vocaby.app.api

import com.vocaby.app.models.dictionary.EntryModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("dictionary/{entry}")
    suspend fun getDefinitions(@Path("entry") entry: String): Response<EntryModel>

    @GET("dictionary/update-check/{entry}")
    suspend fun checkEntryUpdate(@Path("entry") entry: String): Response<String>

    @GET("wod/get/{date}/")
    suspend fun getWoD(@Path("date") date: String): Response<EntryModel>
}
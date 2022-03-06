package com.vocaby.application.feature_dictionary.data.remote

import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("dictionary/get/{entry}/{date}/")
    suspend fun checkAndGetDefinitions(@Path("entry") entry: String, @Path("date") date: String): Response<EntryModel>

    @GET("eod/get/{date}/")
    suspend fun getWoD(@Path("date") date: String): Response<EntryModel>
}
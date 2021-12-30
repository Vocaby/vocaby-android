package com.vocaby.app.api

import com.google.gson.GsonBuilder
import com.vocaby.app.Constants.VOCABY_API_BASE_URL
import com.vocaby.app.models.dictionary.EntryModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiManager {
    val apiService by lazy {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(EntryModel::class.java, EntryDeserializer())
        val vocabyGson = gsonBuilder.create()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()


        Retrofit.Builder()
            .baseUrl(VOCABY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(vocabyGson))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
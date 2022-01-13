package com.vocaby.application.api

import com.google.gson.GsonBuilder
import com.vocaby.application.Constants.VOCABY_API_BASE_URL
import com.vocaby.application.models.dictionary.EntryModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiManager {
    val apiService: ApiService by lazy {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(EntryModel::class.java, EntryDeserializer())
        val vocabyGson = gsonBuilder.create()
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.apply {
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        }

        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(VOCABY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(vocabyGson))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
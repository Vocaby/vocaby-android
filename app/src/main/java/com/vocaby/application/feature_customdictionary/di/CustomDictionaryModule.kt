package com.vocaby.application.feature_customdictionary.di

import com.google.gson.GsonBuilder
import com.vocaby.application.core.Constants
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_customdictionary.data.CustomDictionaryRepositoryImpl
import com.vocaby.application.feature_customdictionary.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary.data.remote.DictionaryApi
import com.vocaby.application.feature_dictionary.data.remote.EntryDeserializer
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CustomDictionaryModule {
    @Provides
    @Singleton
    fun provideCustomDictionaryRepository(
        database: VocabyDatabase
    ): CustomDictionaryRepository {
        return CustomDictionaryRepositoryImpl(
            database.customDictionaryDao
        )
    }

    @Provides
    @Singleton
    fun provideDictionaryApi(): DictionaryApi {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(EntryModel::class.java, EntryDeserializer())
        val vocabyGson = gsonBuilder.create()
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.apply {
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        }

        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.VOCABY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(vocabyGson))
            .client(okHttpClient)
            .build()
            .create(DictionaryApi::class.java)
    }
}
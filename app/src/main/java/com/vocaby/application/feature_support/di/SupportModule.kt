package com.vocaby.application.feature_support.di

import com.vocaby.application.feature_support.data.SupportRepositoryImpl
import com.vocaby.application.feature_support.data.remote.SupportApi
import com.vocaby.application.feature_support.domain.repository.SupportRepository
import com.vocaby.application.feature_support.domain.use_case.GetFaqUseCase
import com.vocaby.application.feature_support.domain.use_case.SubmitFeedbackUseCase
import com.vocaby.application.feature_support.domain.use_case.SupportUseCases
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
class SupportModule {
    @Provides
    @Singleton
    fun provideSupportApi(): SupportApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(com.vocaby.application.core.Constants.VOCABY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(SupportApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSupportRepository(
        supportApi: SupportApi
    ): SupportRepository = SupportRepositoryImpl(supportApi)

    @Provides
    @Singleton
    fun provideSupportUseCases(
        supportRepository: SupportRepository
    ): SupportUseCases = SupportUseCases(
        GetFaqUseCase(supportRepository),
        SubmitFeedbackUseCase(supportRepository)
    )
}
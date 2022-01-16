package com.vocaby.application.feature_user.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import com.vocaby.application.core.Constants
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_user.data.DataManager
import com.vocaby.application.feature_user.data.UserRepositoryImpl
import com.vocaby.application.feature_user.data.remote.UserApi
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserModule {
    @Provides
    @Singleton
    @Named("user")
    fun provideUserSharedPref(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideUserRepository(
        database: VocabyDatabase,
        userApi: UserApi,
        dataManager: DataManager,
        @Named("user")
        userSharedPref: SharedPreferences,
        contentResolver: ContentResolver
    ): UserRepository {
        return UserRepositoryImpl(
            database.userDao,
            userApi,
            dataManager,
            userSharedPref,
            contentResolver
        )
    }

    @Provides
    @Singleton
    fun provideUserApi(): UserApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.VOCABY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDataManager(@ApplicationContext context: Context): DataManager = DataManager(context)
}
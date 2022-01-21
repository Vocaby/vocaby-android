package com.vocaby.application.feature_user.di

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_user.common.Constants
import com.vocaby.application.feature_user.data.UserRepositoryImpl
import com.vocaby.application.feature_user.domain.repository.UserRepository
import com.vocaby.application.feature_user.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        @Named("user")
        userSharedPref: SharedPreferences
    ): UserRepository {
        return UserRepositoryImpl(
            database.userDao,
            userSharedPref
        )
    }

    @Provides
    @Singleton
    fun provideUserUseCases(
        userRepository: UserRepository,
        @Named("placeholderColors")
        placeholderColors: List<Int>,
        @Named("chartColors")
        chartColors: List<Int>
    ): ProfileUseCases = ProfileUseCases(
        UpdateChartUseCase(userRepository, placeholderColors, chartColors),
        UpdateChartModeUseCase(userRepository),
        GetChartModeUseCase(userRepository),
        EraseChartDataUseCase(userRepository),
        SetupUserUseCase(userRepository)
    )

    @Provides
    @Singleton
    @Named("placeholderColors")
    fun providePlaceHolderColors(): List<Int> = listOf(
        Color.parseColor("#C1C1C1"),
        Color.parseColor("#C8C8C8"),
        Color.parseColor("#CFCFCF"),
        Color.parseColor("#D6D6D6"),
        Color.parseColor("#DCDCDC")
    )

    @Provides
    @Singleton
    @Named("chartColors")
    fun provideChartColors(): List<Int> = listOf(
        Color.parseColor("#7BB38D"),
        Color.parseColor("#89BB99"),
        Color.parseColor("#A6CCB0"),
        Color.parseColor("#C3DDC7"),
        Color.parseColor("#D1E5D3")
    )
}
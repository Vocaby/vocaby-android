package com.vocaby.application.feature_profile.di

import android.content.Context
import android.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.vocaby.app.UserSettings
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_profile.data.UserRepositoryImpl
import com.vocaby.application.feature_profile.domain.model.NotificationFrequency
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_profile.domain.use_case.*
import com.vocaby.application.feature_profile.presentation.setting.UserSettingsSerializer
import com.vocaby.application.feature_save.domain.repository.SaveRepository
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
    fun provideUserRepository(
        database: VocabyDatabase,
        userSettings: DataStore<UserSettings>
    ): UserRepository {
        return UserRepositoryImpl(
            database.userDao,
            userSettings
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
        GetProfileDataUseCase(userRepository),
        GetChartSettingsUseCase(userRepository),
        EraseChartDataUseCase(userRepository),
        SetupBaseUserUseCase(userRepository),
        CleanUpUserUseCase(userRepository),
        ResetUseCase(userRepository)
    )

    private val Context.userSettingsDataStore: DataStore<UserSettings> by dataStore(
        fileName = "base_user_settings.pb",
        serializer = UserSettingsSerializer
    )

    @Provides
    @Singleton
    fun provideUserSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserSettings> = context.userSettingsDataStore

    @Provides
    @Singleton
    fun provideSettingsUseCase(
        userRepository: UserRepository,
        saveRepository: SaveRepository,
        notificationFrequencies: Array<NotificationFrequency>,
    ): SettingsUseCases = SettingsUseCases(
        GetUserSettingsUseCase(userRepository),
        UpdateChartModeUseCase(userRepository),
        GetNotificationSettingsUseCase(userRepository),
        GetNotificationFrequenciesUseCase(notificationFrequencies),
        GetSelectedNotificationCollectionUseCase(saveRepository),
        GetSelectedNotificationFrequencyUseCase(),
        UpdateNotificationSettingsUseCase(userRepository),
        UpdateNotificationCollectionUseCase(userRepository),
        UpdateNotificationFrequencyUseCase(userRepository),
        UpdateConnectionSettingsUseCase(userRepository),
        UpdateDataShareSettingsUseCase(userRepository)
    )

    @Provides
    @Singleton
    fun provideNotificationFrequencies(): Array<NotificationFrequency> = NotificationFrequency.values()

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
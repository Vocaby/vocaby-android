package com.vocaby.application.feature_save.di

import com.vocaby.application.feature_save.domain.use_cases.*
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SaveModule {
    @Provides
    @Singleton
    fun provideSaveUseCases(
        userRepository: UserRepository
    ): SaveUseCases = SaveUseCases(
        GetUserSavesUseCase(userRepository),
        AddSaveItemUseCase(userRepository),
        RemoveSaveItemUseCase(userRepository),
        ClearUserSavesUseCase(userRepository)
    )
}
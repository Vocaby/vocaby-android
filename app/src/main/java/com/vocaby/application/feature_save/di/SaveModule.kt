package com.vocaby.application.feature_save.di

import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.SaveRepositoryImpl
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import com.vocaby.application.feature_save.domain.use_cases.collection.GetSaveCollectionsUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.SaveCollectionUseCases
import com.vocaby.application.feature_save.domain.use_cases.save.ClearUserSavesUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.GetUserSavesUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.RemoveSaveItemUseCase
import com.vocaby.application.feature_save.domain.use_cases.save.SaveUseCases
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
    fun provideSaveRepository(
        vocabyDatabase: VocabyDatabase
    ): SaveRepository = SaveRepositoryImpl(
        vocabyDatabase.saveDao
    )

    @Provides
    @Singleton
    fun provideSaveUseCases(
        userRepository: UserRepository,
        saveRepository: SaveRepository
    ): SaveUseCases = SaveUseCases(
        GetUserSavesUseCase(userRepository, saveRepository),
        RemoveSaveItemUseCase(userRepository,saveRepository),
        ClearUserSavesUseCase(userRepository, saveRepository)
    )

    @Provides
    @Singleton
    fun provideSaveCollectionUseCase(
        userRepository: UserRepository,
        saveRepository: SaveRepository
    ): SaveCollectionUseCases = SaveCollectionUseCases(
        GetSaveCollectionsUseCase(userRepository, saveRepository)
    )
}
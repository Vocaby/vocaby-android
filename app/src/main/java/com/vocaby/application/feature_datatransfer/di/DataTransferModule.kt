package com.vocaby.application.feature_datatransfer.di

import android.content.ContentResolver
import com.vocaby.application.feature_datatransfer.data.DataTransferRepositoryImpl
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.domain.use_case.*
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataTransferModule {
    @Provides
    @Singleton
    fun provideDataTransferUseCases(
        userRepository: UserRepository,
        customDictionaryRepository: CustomDictionaryRepository,
        saveRepository: SaveRepository,
        dataTransferRepository: DataTransferRepository
    ): DataTransferUseCases = DataTransferUseCases(
        ImportSavesUseCase(userRepository, saveRepository, dataTransferRepository),
        ImportCustomEntriesUseCase(userRepository, customDictionaryRepository, dataTransferRepository),
        ExportSavesUseCase(userRepository, saveRepository, dataTransferRepository),
        ExportCustomEntriesUseCase(userRepository, customDictionaryRepository, dataTransferRepository)
    )

    @Provides
    @Singleton
    fun provideDataTransferRepository(
        contentResolver: ContentResolver,
    ): DataTransferRepository = DataTransferRepositoryImpl(
        contentResolver
    )
}
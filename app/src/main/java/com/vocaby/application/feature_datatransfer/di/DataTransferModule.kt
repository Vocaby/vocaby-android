package com.vocaby.application.feature_datatransfer.di

import android.content.ContentResolver
import com.vocaby.application.feature_datatransfer.data.DataTransferRepositoryImpl
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.domain.use_case.*
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
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
        dataTransferRepository: DataTransferRepository,
        availableTypes: List<Type>
    ): DataTransferUseCases = DataTransferUseCases(
        ImportSavesUseCase(userRepository, dataTransferRepository),
        ImportCustomEntriesUseCase(userRepository, customDictionaryRepository, dataTransferRepository, availableTypes),
        ExportSavesUseCase(userRepository, dataTransferRepository),
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
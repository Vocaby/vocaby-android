package com.vocaby.application.feature_dictionary_custom.di

import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_dictionary_custom.data.CustomDictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideCustomEntryUseCases(
        customDictionaryRepository: CustomDictionaryRepository
    ): CustomEntryUseCases {
        return CustomEntryUseCases(
            GetCustomEntriesUseCase(customDictionaryRepository),
            FilterCustomEntriesUseCase(customDictionaryRepository),
            RemoveCustomEntryUseCase(customDictionaryRepository),
            ValidateCustomEntryUseCase(),
            RemoveUserEntriesUseCase(customDictionaryRepository)
        )
    }
}
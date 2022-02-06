package com.vocaby.application.feature_dictionary_custom.di

import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.data.CustomDictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.EntryBuilderUseCases
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.GetCustomEntryUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.GetTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.home.*
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

    @Provides
    @Singleton
    fun provideEntryBuilderUseCases(
        customDictionaryRepository: CustomDictionaryRepository
    ): EntryBuilderUseCases {
        return EntryBuilderUseCases(
            GetCustomEntryUseCase(customDictionaryRepository),
            GetTypesUseCase(customDictionaryRepository)
        )
    }


    @Provides
    @Singleton
    fun provideFactoryTypes(): List<Type> {
        return listOf(
            Type("noun", 0),
            Type("verb", 1),
            Type("adjective",2),
            Type("adverb",3),
            Type("idiom",4),
            Type("proverb",5),
            Type("phrase", 6),
            Type("preposition",7),
            Type("interjection",8),
            Type("conjunction", 9),
            Type("pronoun", 10),
        )
    }
}
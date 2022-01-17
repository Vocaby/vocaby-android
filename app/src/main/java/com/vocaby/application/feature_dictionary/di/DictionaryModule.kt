package com.vocaby.application.feature_dictionary.di

import android.content.Context
import android.content.SharedPreferences
import com.vocaby.application.core.Constants
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.feature_dictionary.data.DictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary.data.remote.DictionaryApi
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.domain.use_case.*
import com.vocaby.application.feature_dictionary.data.DataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DictionaryModule {
    @Provides
    @Singleton
    fun provideDictionaryRepository(
        database: VocabyDatabase,
        dictionaryApi: DictionaryApi,
        dataManager: DataManager,
        @Named("dictionary")
        dictionarySharedPref: SharedPreferences
    ): DictionaryRepository {
        return DictionaryRepositoryImpl(
            database.dictionaryDao,
            dictionaryApi,
            dataManager,
            dictionarySharedPref
        )
    }

    @Provides
    @Singleton
    @Named("dictionary")
    fun provideDictionarySharedPref(@ApplicationContext context: Context): SharedPreferences
            = context.getSharedPreferences(Constants.DICTIONARY_SHARED_PREF_ID, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideDataManager(@ApplicationContext context: Context): DataManager = DataManager(context)

    @Provides
    @Singleton
    fun provideDictionaryUseCases(
        dictionaryRepository: DictionaryRepository,
        applicationRepository: ApplicationRepository
    ): DictionaryUseCases = DictionaryUseCases(
        GetSearchHistoryUseCase(dictionaryRepository),
        GetSearchHistoryItemUseCase(dictionaryRepository),
        ValidateSearchUserCase(),
        GetDictionaryEntriesByCharacter(dictionaryRepository),
        GetSearchSuggestionsUseCase(),
        GetDailyPick(dictionaryRepository, applicationRepository),
        EraseSearchHistoryUserCase(dictionaryRepository),
        InsertSearchHistoryUseCase(dictionaryRepository),
        ClearDictionaryCacheUseCase(dictionaryRepository)
    )
}
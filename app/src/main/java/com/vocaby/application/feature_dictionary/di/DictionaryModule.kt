package com.vocaby.application.feature_dictionary.di

import android.content.SharedPreferences
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_dictionary.data.DictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary.data.remote.DictionaryApi
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        @Named("application")
        applicationSharedPref: SharedPreferences,
        @Named("cache")
        cacheSharedPref: SharedPreferences
    ): DictionaryRepository {
        return DictionaryRepositoryImpl(
            database.dictionaryDao,
            dictionaryApi,
            applicationSharedPref,
            cacheSharedPref
        )
    }
}
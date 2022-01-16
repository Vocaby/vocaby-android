package com.vocaby.application.core.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.vocaby.application.core.Constants
import com.vocaby.application.core.data.VocabyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {
    @Provides
    @Singleton
    fun provideVocabyDatabase(@ApplicationContext context: Context): VocabyDatabase
        = VocabyDatabase.getDatabase(context)

    @Provides
    @Singleton
    @Named("application")
    fun provideApplicationSharedPref(@ApplicationContext context: Context): SharedPreferences
        = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    @Named("cache")
    fun provideCacheSharedPref(@ApplicationContext context: Context): SharedPreferences
            = context.getSharedPreferences(Constants.DICTIONARY_CACHE_ID, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver
}
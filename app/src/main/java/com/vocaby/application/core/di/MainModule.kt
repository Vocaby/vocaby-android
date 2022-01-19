package com.vocaby.application.core.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.vocaby.application.core.data.ApplicationRepositoryImpl
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.feature_dictionary.data.local.entity.Type
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
    fun provideVocabyDatabase(
        @ApplicationContext context: Context,
        availableTypes: List<Type>
    ): VocabyDatabase = VocabyDatabase.getDatabase(context, availableTypes)

    @Provides
    @Singleton
    @Named("application")
    fun provideApplicationSharedPref(@ApplicationContext context: Context): SharedPreferences
        = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideApplicationRepository(
        @Named("application")
        applicationSharedPref: SharedPreferences
    ): ApplicationRepository {
        return ApplicationRepositoryImpl(
            applicationSharedPref
        )
    }

    @Provides
    @Singleton
    fun provideAvailableTypes(): List<Type> {
        return listOf(
            Type("noun"),
            Type("verb"),
            Type("adjective"),
            Type("adverb"),
            Type("idiom"),
            Type("proverb"),
            Type("phrase"),
            Type("preposition"),
            Type("interjection"),
            Type("conjunction"),
            Type("pronoun"),
        )
    }
}
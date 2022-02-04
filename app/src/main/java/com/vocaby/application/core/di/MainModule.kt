package com.vocaby.application.core.di

import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.vocaby.app.ApplicationData
import com.vocaby.application.core.data.ApplicationRepositoryImpl
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.core.presentation.ApplicationDataSerializer
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    private val Context.applicationDataStore: DataStore<ApplicationData> by dataStore(
        fileName = "application.pb",
        serializer = ApplicationDataSerializer
    )

    @Provides
    @Singleton
    fun provideApplicationDataStore(
        @ApplicationContext context: Context
    ): DataStore<ApplicationData> = context.applicationDataStore

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideApplicationRepository(
        applicationDataStore: DataStore<ApplicationData>
    ): ApplicationRepository {
        return ApplicationRepositoryImpl(
            applicationDataStore
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
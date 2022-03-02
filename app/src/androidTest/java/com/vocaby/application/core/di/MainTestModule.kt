package com.vocaby.application.core.di

import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocaby.app.ApplicationData
import com.vocaby.application.core.data.ApplicationRepositoryImpl
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.core.presentation.ApplicationDataSerializer
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import java.util.*

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [MainModule::class]
)
object MainTestModule {
    @Provides
    fun provideVocabyDatabase(
        @ApplicationContext
        context: Context,
    ): VocabyDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            VocabyDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    private val Context.applicationDataStore: DataStore<ApplicationData> by dataStore(
        fileName = "application.pb",
        serializer = ApplicationDataSerializer
    )

    @Provides
    fun provideApplicationDataStore(
        @ApplicationContext context: Context
    ): DataStore<ApplicationData> = context.applicationDataStore

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    fun provideApplicationRepository(
        applicationDataStore: DataStore<ApplicationData>
    ): ApplicationRepository {
        return ApplicationRepositoryImpl(
            applicationDataStore
        )
    }
}
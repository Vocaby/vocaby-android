package com.vocaby.application.feature_dictionary.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.gson.GsonBuilder
import com.vocaby.app.DictionaryCache
import com.vocaby.application.core.Constants
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.feature_dictionary.data.DataManager
import com.vocaby.application.feature_dictionary.data.DictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary.data.remote.DictionaryApi
import com.vocaby.application.feature_dictionary.data.remote.EntryDeserializer
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.domain.use_case.*
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryCacheSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
        dictionaryCacheDataStore: DataStore<DictionaryCache>
    ): DictionaryRepository {
        return DictionaryRepositoryImpl(
            database.dictionaryDao,
            dictionaryApi,
            dataManager,
            dictionaryCacheDataStore
        )
    }

    private val Context.dictionaryCacheDataStore: DataStore<DictionaryCache> by dataStore(
        fileName = "dictionary_cache.pb",
        serializer = DictionaryCacheSerializer
    )

    @Provides
    @Singleton
    fun provideDictionaryCacheDataStore(
        @ApplicationContext context: Context
    ): DataStore<DictionaryCache> = context.dictionaryCacheDataStore

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
        GetDictionaryEntriesByCharacter(dictionaryRepository),
        GetSearchSuggestionsUseCase(),
        GetDailyPick(dictionaryRepository, applicationRepository),
        GetPrevPick(dictionaryRepository),
        EraseSearchHistoryUserCase(dictionaryRepository),
        InsertSearchHistoryUseCase(dictionaryRepository),
        ClearDictionaryCacheUseCase(dictionaryRepository)
    )

    @Provides
    @Singleton
    fun provideDictionaryApi(): DictionaryApi {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(EntryModel::class.java, EntryDeserializer())
        val vocabyGson = gsonBuilder.create()
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.apply {
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        }

        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.VOCABY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(vocabyGson))
            .client(okHttpClient)
            .build()
            .create(DictionaryApi::class.java)
    }
}
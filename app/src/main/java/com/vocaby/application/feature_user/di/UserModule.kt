package com.vocaby.application.feature_user.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_user.common.Constants
import com.vocaby.application.feature_user.data.UserRepositoryImpl
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserModule {
    @Provides
    @Singleton
    @Named("user")
    fun provideUserSharedPref(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideUserRepository(
        database: VocabyDatabase,
        @Named("user")
        userSharedPref: SharedPreferences,
        contentResolver: ContentResolver,
        availableTypes: List<Type>
    ): UserRepository {
        return UserRepositoryImpl(
            database.userDao,
            userSharedPref,
            contentResolver,
            availableTypes
        )
    }
}
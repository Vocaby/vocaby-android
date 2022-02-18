package com.vocaby.application.feature_profile.domain.use_case

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vocaby.application.core.di.MainModule
import com.vocaby.application.feature_dictionary.di.DictionaryModule
import com.vocaby.application.feature_dictionary_custom.di.CustomDictionaryModule
import com.vocaby.application.feature_profile.di.UserModule
import com.vocaby.application.feature_save.di.SaveModule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupBaseUserUseCaseTest {
    @Before
    fun setup() {
    }

    @Test
    fun testBaseUserSetup() {
    }
}
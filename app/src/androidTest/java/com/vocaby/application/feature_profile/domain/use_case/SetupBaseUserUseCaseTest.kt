package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.data.FakeUserRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class SetupBaseUserUseCaseTest {
    private lateinit var setupBaseUserUseCase: SetupBaseUserUseCase
    private lateinit var userRepository: UserRepository
    @Before
    fun setup() {
        userRepository = FakeUserRepository()
        setupBaseUserUseCase = SetupBaseUserUseCase(userRepository)
    }

    @Test
    fun testBaseUserSetup() {
        runBlocking {
            setupBaseUserUseCase.invoke()
            val user = userRepository.getUser()

            assertTrue(user == 1)
        }
    }
}
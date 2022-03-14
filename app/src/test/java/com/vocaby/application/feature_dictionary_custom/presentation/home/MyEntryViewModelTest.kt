package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.data.FakeCustomDictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary_custom.domain.use_case.home.*
import com.vocaby.application.feature_profile.data.FakeUserRepository
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_profile.domain.use_case.SetupBaseUserUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class MyEntryViewModelTest {
    private lateinit var viewModel: MyEntryViewModel
    private lateinit var customEntryUseCases: CustomEntryUseCases

    @Before
    fun setup() = runTest {
        val customDictionaryRepository = FakeCustomDictionaryRepositoryImpl()
        val userRepository = FakeUserRepository()

        val setupBaseUserUseCase = SetupBaseUserUseCase(userRepository)
        setupBaseUserUseCase.invoke()

        val getCurrentUserUseCase = GetCurrentUserUseCase(userRepository)

        customEntryUseCases = CustomEntryUseCases(
            GetCustomEntriesUseCase(customDictionaryRepository),
            FilterCustomEntriesUseCase(userRepository, customDictionaryRepository),
            GetCustomEntriesCountUseCase(customDictionaryRepository),
            RemoveCustomEntryUseCase(customDictionaryRepository),
            ValidateCustomEntryUseCase(),
            RemoveUserEntriesUseCase(userRepository, customDictionaryRepository)
        )

        viewModel = MyEntryViewModel(customEntryUseCases, getCurrentUserUseCase)
    }
}
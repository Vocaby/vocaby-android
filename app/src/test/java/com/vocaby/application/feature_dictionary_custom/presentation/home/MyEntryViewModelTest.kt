package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.data.FakeCustomDictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.use_case.home.*
import com.vocaby.application.feature_profile.data.FakeUserRepository
import com.vocaby.application.feature_profile.domain.use_case.GetCurrentUserUseCase
import com.vocaby.application.feature_profile.domain.use_case.SetupBaseUserUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Before

class MyEntryViewModelTest {
    private lateinit var viewModel: MyEntryViewModel
    private lateinit var customEntryUseCases: CustomEntryUseCases

    @Before
    fun setup() = runBlocking {
        val customDictionaryRepository = FakeCustomDictionaryRepository()
        val userRepository = FakeUserRepository()

        val setupBaseUserUseCase = SetupBaseUserUseCase(userRepository)
        setupBaseUserUseCase.invoke()

        val getCurrentUserUseCase = GetCurrentUserUseCase(userRepository)

        customEntryUseCases = CustomEntryUseCases(
            GetCustomEntriesUseCase(customDictionaryRepository),
            FilterCustomEntriesUseCase(userRepository, customDictionaryRepository),
            RemoveCustomEntryUseCase(customDictionaryRepository),
            ValidateCustomEntryUseCase(),
            RemoveUserEntriesUseCase(userRepository, customDictionaryRepository)
        )

        viewModel = MyEntryViewModel(customEntryUseCases, getCurrentUserUseCase)
    }
}
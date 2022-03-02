package com.vocaby.application.feature_dictionary.domain.use_case

import com.google.common.truth.Truth
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.FakeDictionaryRepository
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.presentation.search.SearchState
import com.vocaby.application.feature_dictionary_custom.data.FakeCustomDictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary_custom.domain.repository.FakeCustomDictionaryRepository
import com.vocaby.application.feature_profile.data.FakeUserRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetAllDictionaryEntryUseCaseTest {
    private lateinit var getAllDictionaryEntryUseCase: GetAllDictionaryEntryUseCase
    private lateinit var userRepository: UserRepository
    private lateinit var dictionaryRepository: DictionaryRepository
    private lateinit var customDictionaryRepository: FakeCustomDictionaryRepository

    @Before
    fun setup() = runTest {
        userRepository = FakeUserRepository()
        dictionaryRepository = FakeDictionaryRepository()
        customDictionaryRepository = FakeCustomDictionaryRepositoryImpl()

        getAllDictionaryEntryUseCase = GetAllDictionaryEntryUseCase(
            userRepository,
            dictionaryRepository,
            customDictionaryRepository
        )

        userRepository.setupBaseUser()
    }

    @Test
    fun `Only custom entry is displayed`() = runTest {
        val userId = userRepository.getUser()

        customDictionaryRepository.insertEntry(
            userId,
            "test",
            "verb",
            "this is a test",
            ""
        )

        val searchState = getAllDictionaryEntryUseCase.invoke("test").first() as SearchState.Fetched
        Truth.assertThat(searchState.data.availableEntry).isNotNull()
        Truth.assertThat(searchState.data.customModel).isNotNull()
        Truth.assertThat(searchState.data.originalModel).isNull()
        Truth.assertThat(searchState.data.availableEntry!!.definitionGroups.first().definitionData.first().definition).isEqualTo("this is a test")
        Truth.assertThat(searchState.data.customModel!!.definitionGroups.first().definitionData.first().definition).isEqualTo("this is a test")
        Truth.assertThat(searchState.removeSave).isFalse()
        Truth.assertThat(searchState.dictionarySelectorState.displayAll).isFalse()
        Truth.assertThat(searchState.dictionarySelectorState.hideId).isEqualTo(R.id.selection_original)
        Truth.assertThat(searchState.dictionarySelectorState.displayId).isEqualTo(R.id.selection_custom)
    }

    @Test
    fun `Only original entry is displayed`() = runTest {
        val userId = userRepository.getUser()

        customDictionaryRepository.insertEntry(
            userId,
            "test",
            "verb",
            "this is a test",
            ""
        )

        val searchState = getAllDictionaryEntryUseCase.invoke("galvanize").first() as SearchState.Fetched
        Truth.assertThat(searchState.data.availableEntry).isNotNull()
        Truth.assertThat(searchState.data.originalModel).isNotNull()
        Truth.assertThat(searchState.data.customModel).isNull()
        Truth.assertThat(searchState.data.availableEntry!!.definitionGroups).hasSize(1)
        Truth.assertThat(searchState.data.availableEntry!!.definitionGroups.first().definitionData.first().definition).isEqualTo("shock or excite (someone) into taking action.")
        Truth.assertThat(searchState.data.originalModel!!.definitionGroups.first().definitionData.first().definition).isEqualTo("shock or excite (someone) into taking action.")
        Truth.assertThat(searchState.removeSave).isFalse()
        Truth.assertThat(searchState.dictionarySelectorState.displayAll).isFalse()
        Truth.assertThat(searchState.dictionarySelectorState.hideId).isEqualTo(R.id.selection_custom)
        Truth.assertThat(searchState.dictionarySelectorState.displayId).isEqualTo(R.id.selection_original)
    }

    @Test
    fun `Both entries are displayed`() = runTest {
        val userId = userRepository.getUser()

        customDictionaryRepository.insertEntry(
            userId,
            "enthusiasm",
            "verb",
            "this is a test",
            ""
        )

        val searchState = getAllDictionaryEntryUseCase.invoke("enthusiasm").first() as SearchState.Fetched
        Truth.assertThat(searchState.data.availableEntry).isNotNull()
        Truth.assertThat(searchState.data.originalModel).isNotNull()
        Truth.assertThat(searchState.data.customModel).isNotNull()
        Truth.assertThat(searchState.data.availableEntry!!.definitionGroups).hasSize(1)
        Truth.assertThat(searchState.data.availableEntry!!.definitionGroups.first().definitionData.first().definition).isEqualTo("this is a test")
        Truth.assertThat(searchState.data.originalModel!!.definitionGroups.first().definitionData.first().definition).isEqualTo("intense and eager enjoyment, interest, or approval.")
        Truth.assertThat(searchState.removeSave).isFalse()
        Truth.assertThat(searchState.dictionarySelectorState.displayAll).isTrue()
        Truth.assertThat(searchState.dictionarySelectorState.hideId).isEqualTo(R.id.selection_original)
        Truth.assertThat(searchState.dictionarySelectorState.displayId).isEqualTo(R.id.selection_custom)
    }

    @Test
    fun `No entry is displayed`() = runTest {
        val searchState = getAllDictionaryEntryUseCase.invoke("testing").first() as SearchState.Fetched
        Truth.assertThat(searchState.data.availableEntry).isNull()
        Truth.assertThat(searchState.data.originalModel).isNull()
        Truth.assertThat(searchState.data.customModel).isNull()
        Truth.assertThat(searchState.removeSave).isTrue()
        Truth.assertThat(searchState.dictionarySelectorState.displayAll).isFalse()
    }
}
package com.vocaby.application.feature_dictionary_custom.presentation.type

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.data.FakeCustomDictionaryRepositoryImpl
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.use_case.builder.GetTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ModifyTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ResetTypesUseCase
import com.vocaby.application.feature_dictionary_custom.domain.use_case.type.ValidateTypeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TypeManagementViewModelTest {
    private lateinit var factoryTypes: List<Type>
    private lateinit var customDictionaryRepository: CustomDictionaryRepository
    private lateinit var viewModel: TypeManagementViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)

        customDictionaryRepository = FakeCustomDictionaryRepositoryImpl()

        factoryTypes = listOf(
            Type("noun", 0),
            Type("verb", 1),
            Type("adjective", 2),
            Type("adverb", 3),
        )

        customDictionaryRepository.insertTypes(factoryTypes)

        viewModel = TypeManagementViewModel(
            ResetTypesUseCase(customDictionaryRepository, factoryTypes),
            GetTypesUseCase(customDictionaryRepository),
            ValidateTypeUseCase(),
            ModifyTypesUseCase(customDictionaryRepository)
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Types are properly setup`() = runTest {
        viewModel.typeState.test {
            awaitItem()
            val types = awaitItem()

            Truth.assertThat(types).hasSize(4)
            Truth.assertThat(types).containsExactly(
                Type("noun", 0, false, 1),
                Type("verb", 1, false , 2),
                Type("adjective", 2, false, 3),
                Type("adverb", 3, false, 4),
            )

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Type is properly added`() = runTest {
        viewModel.typeState.test {
            awaitItem()
            awaitItem()

            viewModel.createType("pronoun")

            delay(100)
            Truth.assertThat(viewModel.typeChangeState.hasChanges()).isTrue()

            viewModel.save()
            viewModel.getTypes()
            val types = awaitItem()

            Truth.assertThat(types).hasSize(5)
            Truth.assertThat(types).containsExactly(
                Type("pronoun", 0, true, 5),
                Type("noun", 1, false, 1),
                Type("verb", 2, false , 2),
                Type("adjective", 3, false, 3),
                Type("adverb", 4, false, 4),
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Type is properly deleted`() = runTest {
        viewModel.typeState.test {
            awaitItem()
            awaitItem()

            viewModel.removeType(0)

            delay(100)
            Truth.assertThat(viewModel.typeChangeState.deletedItems).contains(
                Type(type="noun", order=0, userDefined=false, typeId=1)
            )

            viewModel.save()
            viewModel.getTypes()

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Type is properly added and deleted`() = runTest {
        viewModel.typeState.test {
            awaitItem()
            awaitItem()

            viewModel.createType("pronoun")
            viewModel.removeType(0)

            Truth.assertThat(viewModel.typeChangeState.hasChanges()).isFalse()

            viewModel.save()
            viewModel.getTypes()
            expectNoEvents()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Type has proper order`() = runTest {
        viewModel.typeState.test {
            awaitItem()
            awaitItem()

            viewModel.createType("pronoun")
            viewModel.createType("conjunction")
            viewModel.removeType(3)
            viewModel.removeType(2)

            viewModel.save()
            viewModel.getTypes()
            val types = awaitItem()

            Truth.assertThat(types).hasSize(4)
            Truth.assertThat(types).containsExactly(
                Type("conjunction", 0, true, 6),
                Type("pronoun", 1, true, 5),
                Type("adjective", 2, false, 3),
                Type("adverb", 3, false, 4)
            )

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }
}
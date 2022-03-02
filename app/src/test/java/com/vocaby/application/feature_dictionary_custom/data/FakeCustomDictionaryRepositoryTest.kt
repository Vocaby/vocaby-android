package com.vocaby.application.feature_dictionary_custom.data


import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before

import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeCustomDictionaryRepositoryTest {
    private lateinit var customDictionaryRepository: FakeCustomDictionaryRepositoryImpl

    @Before
    fun setUp() {
        customDictionaryRepository = FakeCustomDictionaryRepositoryImpl()
    }


    @Test
    fun `Custom entry is properly inserted`() = runTest {
        customDictionaryRepository.insertEntry(
            1,
            "galvanize",
            "verb",
            "shock or excite (someone) into taking action.",
            "\"the urgency of his voice galvanized them into action\"",
            "coat (iron or steel) with a protective layer of zinc.",
            null
        )

        var entryModels = customDictionaryRepository.getAllUserEntries(1)
        Truth.assertThat(entryModels).hasSize(1)
        Truth.assertThat(entryModels.first().entry).isEqualTo("galvanize")
        Truth.assertThat(entryModels.first().id).isEqualTo(1)
        Truth.assertThat(entryModels.first().definitionGroups).hasSize(1)
        Truth.assertThat(entryModels.first().definitionGroups.first().type).isEqualTo("verb")
        Truth.assertThat(entryModels.first().definitionGroups.first().groupId).isEqualTo(1)
        Truth.assertThat(entryModels.first().definitionGroups.first().definitionData).hasSize(2)
        Truth.assertThat(entryModels.first().definitionGroups.first().definitionData.first().definition).isEqualTo("shock or excite (someone) into taking action.")
        Truth.assertThat(entryModels.first().definitionGroups.first().definitionData.last().definition).isEqualTo("coat (iron or steel) with a protective layer of zinc.")
        Truth.assertThat(entryModels.first().definitionGroups.first().definitionData.last().example).isEqualTo(null)

        customDictionaryRepository.insertEntry(
            1,
            "enthusiasm",
            "noun",
            "This is a custom definition",
            "This is a custom example",
        )

        entryModels = customDictionaryRepository.getAllUserEntries(1)
        Truth.assertThat(entryModels).hasSize(2)
        Truth.assertThat(entryModels.last().entry).isEqualTo("enthusiasm")
        Truth.assertThat(entryModels.last().id).isEqualTo(2)
        Truth.assertThat(entryModels.last().definitionGroups).hasSize(1)
        Truth.assertThat(entryModels.last().definitionGroups.first().type).isEqualTo("noun")
        Truth.assertThat(entryModels.last().definitionGroups.first().groupId).isEqualTo(2)
        Truth.assertThat(entryModels.last().definitionGroups.first().definitionData).hasSize(1)
        Truth.assertThat(entryModels.last().definitionGroups.first().definitionData.first().id).isEqualTo(3)
        Truth.assertThat(entryModels.last().definitionGroups.first().definitionData.first().definition).isEqualTo("This is a custom definition")
    }

    @Test
    fun `Custom entry is properly deleted`() = runTest {
        customDictionaryRepository.insertEntry(
            1,
            "galvanize",
            "verb",
            "shock or excite (someone) into taking action.",
            "\"the urgency of his voice galvanized them into action\"",
            "coat (iron or steel) with a protective layer of zinc.",
            null
        )

        customDictionaryRepository.insertEntry(
            1,
            "enthusiasm",
            "noun",
            "This is a custom entry",
            "This is a custom definition",
        )

        customDictionaryRepository.removeUserEntry("enthusiasm")
        val entryModels = customDictionaryRepository.getAllUserEntries(1)
        Truth.assertThat(entryModels).hasSize(1)
        Truth.assertThat(entryModels.first().entry).isEqualTo("galvanize")

        customDictionaryRepository.removeUserEntry("galvanize")
        val count = customDictionaryRepository.getUserEntriesCount(1).first()
        Truth.assertThat(count).isEqualTo(0)
    }

    @Test
    fun `Custom entry is properly updated`() = runTest {
        customDictionaryRepository.insertEntry(
            1,
            "galvanize",
            "verb",
            "shock or excite (someone) into taking action.",
            "\"the urgency of his voice galvanized them into action\"",
            "coat (iron or steel) with a protective layer of zinc.",
            null
        )

        customDictionaryRepository.insertEntry(
            1,
            "enthusiasm",
            "noun",
            "This is a custom definition",
            "This is a custom example",
        )

        var entryModel = customDictionaryRepository.getUserEntryData(1, "enthusiasm")

        entryModel?.let { model ->
            customDictionaryRepository.updateEntry(
                1,
                model,
                "This is a new definition",
                ""
            )
        }

        entryModel = customDictionaryRepository.getUserEntryData(1, "enthusiasm")
        entryModel?.let {
            Truth.assertThat(entryModel.entry).isEqualTo("enthusiasm")
            Truth.assertThat(entryModel.id).isEqualTo(2)
            Truth.assertThat(entryModel.definitionGroups).hasSize(1)
            Truth.assertThat(entryModel.definitionGroups.first().type).isEqualTo("noun")
            Truth.assertThat(entryModel.definitionGroups.first().groupId).isEqualTo(2)
            Truth.assertThat(entryModel.definitionGroups.first().definitionData).hasSize(1)
            Truth.assertThat(entryModel.definitionGroups.first().definitionData.first().definition).isEqualTo("This is a new definition")
        }
    }
}
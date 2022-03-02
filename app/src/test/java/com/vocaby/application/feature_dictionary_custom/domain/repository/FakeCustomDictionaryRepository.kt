package com.vocaby.application.feature_dictionary_custom.domain.repository

import com.vocaby.application.feature_dictionary.domain.model.EntryModel

interface FakeCustomDictionaryRepository: CustomDictionaryRepository {
    suspend fun insertEntry(
        userId: Int,
        entry: String,
        type: String,
        definition: String,
        example: String,
        definition2: String? = null,
        example2: String? = null
    )
    suspend fun updateEntry(
        userId: Int,
        entryModel: EntryModel,
        newDefinition: String,
        newExample: String
    )
}
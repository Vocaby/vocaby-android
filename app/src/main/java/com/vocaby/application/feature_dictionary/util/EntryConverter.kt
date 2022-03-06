package com.vocaby.application.feature_dictionary.util

import com.vocaby.application.core.util.EntityConverter
import com.vocaby.application.feature_dictionary.data.local.entity.EntryDefinitions
import com.vocaby.application.feature_dictionary.domain.model.EntryModel

object EntryConverter: EntityConverter<EntryDefinitions, EntryModel> {
    override suspend fun convertFromEntity(entity: EntryDefinitions): EntryModel {
        val pronunciation =
            entity.entryData.pronunciation?.let { entity.entryData.pronunciation }
                ?: ""

        val wordData = EntryModel(
            entity.entryData.id,
            entity.entryData.entry,
            pronunciation,
            "",
            entity.entryData.lastUpdated
        )

        for (data in entity.definitions) {
            if (data.definition.isNotEmpty())
                wordData.addDefinition(data.type, data.definition, data.example)
        }

        return wordData
    }
}
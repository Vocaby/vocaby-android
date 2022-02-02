package com.vocaby.application.feature_dictionary.util

import com.vocaby.application.core.util.EntityConverter
import com.vocaby.application.feature_dictionary.data.local.entity.WordDefinitions
import com.vocaby.application.feature_dictionary.domain.model.EntryModel

object EntryConverter: EntityConverter<WordDefinitions, EntryModel> {
    override suspend fun convertFromEntity(entity: WordDefinitions): EntryModel {
        val pronunciation =
            entity.wordData.pronunciation?.let { entity.wordData.pronunciation }
                ?: ""

        val wordData = EntryModel(
            entity.wordData.id,
            entity.wordData.word,
            pronunciation,
            entity.wordData.lastUpdated
        )

        for (data in entity.definitions) {
            wordData.addDefinition(data.pos, data.definition, data.sentence)
        }

        return wordData
    }
}
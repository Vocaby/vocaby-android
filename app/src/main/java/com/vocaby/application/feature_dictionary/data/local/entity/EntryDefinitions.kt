package com.vocaby.application.feature_dictionary.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EntryDefinitions(
    @Embedded val entryData: Entry,
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id",
    )
    val definitions: List<Definition>
)


package com.vocaby.application.feature_dictionary_custom.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EntryWithData(
    @Embedded
    val customEntry: CustomEntry,
    @Relation(
        parentColumn = "custom_entry_id",
        entityColumn = "custom_entry_id",
        entity = CustomEntryGroup::class
    )
    val groups: List<EntryGroupWithDefinitions>
)
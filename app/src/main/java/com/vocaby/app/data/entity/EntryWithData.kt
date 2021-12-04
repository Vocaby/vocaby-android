package com.vocaby.app.data.entity

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
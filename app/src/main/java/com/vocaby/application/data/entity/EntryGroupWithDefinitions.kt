package com.vocaby.application.data.entity
import androidx.room.Embedded
import androidx.room.Relation

data class EntryGroupWithDefinitions(
    @Embedded
    val entryGroup: CustomEntryGroup,
    @Relation(
        parentColumn = "custom_entry_group_id",
        entityColumn = "custom_entry_group_id",
        entity = CustomDefinition::class
    )
    val definitions: List<CustomDefinition>
)

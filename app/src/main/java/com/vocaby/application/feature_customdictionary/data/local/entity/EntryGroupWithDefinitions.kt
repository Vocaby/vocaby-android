package com.vocaby.application.feature_customdictionary.data.local.entity
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

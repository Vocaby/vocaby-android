package com.vocaby.application.data.entity

import androidx.room.*

@Entity(
    tableName = "custom_user_definition",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = CustomEntryGroup::class,
        parentColumns = ["custom_entry_group_id"],
        childColumns = ["custom_entry_group_id"]
    )],
    indices = [Index("custom_entry_group_id")]
)
class CustomDefinition : Comparable<CustomDefinition> {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_definition_id")
    var definitionId = 0

    @ColumnInfo(name = "custom_entry_group_id")
    var groupId: Int
    var definition: String
    var example: String?
    var order: Int

    @Ignore
    constructor(groupId: Int, definition: String, example: String?, order: Int) {
        this.groupId = groupId
        this.definition = definition
        this.order = order
        this.example = example
    }

    constructor(definitionId: Int, groupId: Int, definition: String, example: String?, order: Int) {
        this.definitionId = definitionId
        this.groupId = groupId
        this.definition = definition
        this.order = order
        this.example = example
    }

    override fun compareTo(other: CustomDefinition): Int {
        return order.compareTo(other.order)
    }
}
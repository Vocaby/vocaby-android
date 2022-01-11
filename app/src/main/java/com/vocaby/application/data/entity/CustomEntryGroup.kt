package com.vocaby.application.data.entity

import androidx.room.*

@Entity(
    tableName = "custom_entry_group",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = CustomEntry::class,
        parentColumns = ["custom_entry_id"],
        childColumns = ["custom_entry_id"]
    )],
    indices = [Index("custom_entry_id")]
)
class CustomEntryGroup {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_group_id")
    var groupId = 0

    @ColumnInfo(name = "custom_entry_id")
    var entryId = 0

    @ColumnInfo(name = "type")
    var type: String
    var order = 0

    constructor(groupId: Int, type: String) {
        this.groupId = groupId
        this.type = type
    }

    @Ignore
    constructor(groupId: Int, entryId: Int, type: String, order: Int) {
        this.groupId = groupId
        this.entryId = entryId
        this.type = type
        this.order = order
    }

    @Ignore
    constructor(entryId: Int, type: String, order: Int) {
        this.entryId = entryId
        this.type = type
        this.order = order
    }
}
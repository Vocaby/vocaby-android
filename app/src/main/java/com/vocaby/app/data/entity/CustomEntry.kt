package com.vocaby.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_user_entry", indices = [Index("entry")])
class CustomEntry constructor (
    @ColumnInfo(name = "user_id")
    var userId: Int,
    var entry: String,
    var pronunciation: String,
    @ColumnInfo(name = "last_updated")
    var lastUpdated: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_id")
    var entryId: Int = 0

    constructor(entryId:Int, userId: Int, entry: String, pronunciation: String, lastUpdated: Long)
            : this(userId, entry, pronunciation, lastUpdated) {
        this.entryId = entryId
    }
}
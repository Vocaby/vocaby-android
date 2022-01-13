package com.vocaby.application.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_user_entry", indices = [Index("entry", unique = true)])
data class CustomEntry(
    @ColumnInfo(name = "user_id")
    var userId: Int,
    var entry: String,
    var pronunciation: String?,
    @ColumnInfo(name = "last_updated")
    var lastUpdated: String,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_id")
    var entryId: Int = 0,
)
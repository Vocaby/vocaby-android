package com.vocaby.application.feature_dictionary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entry_type")
data class Type(
    @ColumnInfo(index = true)
    var type: String,
    var order: Int,
    var userDefined: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "type_id", index = true)
    var typeId: Int = 0
)
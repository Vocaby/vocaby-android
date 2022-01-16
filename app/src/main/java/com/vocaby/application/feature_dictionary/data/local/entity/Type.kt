package com.vocaby.application.feature_dictionary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entry_type")
class Type(var type: String) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "type_id")
    var typeId = 0
}
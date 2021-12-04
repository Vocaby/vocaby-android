package com.vocaby.app.data.entity

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "entry_type")
class Type(var type: String) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "type_id")
    var typeId = 0
}
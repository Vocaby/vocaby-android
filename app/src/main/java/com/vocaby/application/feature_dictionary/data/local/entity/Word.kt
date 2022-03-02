package com.vocaby.application.feature_dictionary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "dictionary_word", indices = [Index(value=["word", "id"])])
data class Word constructor(
    var word: String = "",
    var pronunciation:String? = null,
    @ColumnInfo(name = "last_updated")
    var lastUpdated: Date = Date(),
    @PrimaryKey(autoGenerate = true) var id: Int = 0
)
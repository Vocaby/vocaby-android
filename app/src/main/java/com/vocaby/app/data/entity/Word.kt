package com.vocaby.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_word", indices = [Index("word")])
class Word {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var word = ""
    var pronunciation:String? = null
    @ColumnInfo(name = "last_updated")
    var lastUpdated:String = ""
}
package com.vocaby.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_word", indices = [Index("word")])
data class Word constructor(@PrimaryKey(autoGenerate = true) var id: Int) {
    var word = ""
    var pronunciation:String? = null
    @ColumnInfo(name = "last_updated")
    var lastUpdated:String = ""

    constructor(word: String, pronunciation: String?, lastUpdated: String) : this(0) {
        this.word = word
        this.pronunciation = pronunciation
        this.lastUpdated = lastUpdated
    }
}
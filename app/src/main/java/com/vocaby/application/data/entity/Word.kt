package com.vocaby.application.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "dictionary_word", indices = [Index("word")])
data class Word constructor(@PrimaryKey(autoGenerate = true) var id: Int) {
    var word = ""
    var pronunciation:String? = null
    @ColumnInfo(name = "last_updated")
    var lastUpdated: Date = Date()

    constructor(word: String, pronunciation: String?, lastUpdated: Date) : this(0) {
        this.word = word
        this.pronunciation = pronunciation
        this.lastUpdated = lastUpdated
    }
}
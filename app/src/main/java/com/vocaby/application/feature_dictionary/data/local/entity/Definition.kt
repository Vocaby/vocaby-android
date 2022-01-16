package com.vocaby.application.feature_dictionary.data.local.entity

import androidx.room.*

@Entity(
    tableName = "dictionary_definition",
    foreignKeys = [ForeignKey(
        entity = Word::class,
        parentColumns = ["id"],
        childColumns = ["word_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["word_id"])]
)
data class Definition(@PrimaryKey(autoGenerate = true) var id: Int) {
    constructor(wordId: Int, definition: String, sentence: String?, pos: String) : this(0) {
        this.wordId = wordId
        this.definition = definition
        this.sentence = sentence
        this.pos = pos
    }

    @ColumnInfo(name = "word_id")
    var wordId = 0
    var definition: String = ""
    var pos: String = ""
    var sentence: String? = null
}
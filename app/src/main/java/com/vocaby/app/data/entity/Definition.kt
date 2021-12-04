package com.vocaby.app.data.entity

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
class Definition {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "word_id")
    var wordId = 0
    var definition: String = ""
    var pos: String = ""
    var sentence: String? = null
}
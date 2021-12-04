package com.vocaby.app.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WordDefinitions(
    @Embedded val wordData: Word,
    @Relation(
        parentColumn = "id",
        entityColumn = "word_id",
    )
    val definitions: List<Definition>
)


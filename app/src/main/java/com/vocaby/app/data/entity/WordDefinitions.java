package com.vocaby.app.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class WordDefinitions {
    @Embedded
    public Word word;

    @Relation(
            parentColumn = "id",
            entityColumn = "word_id",
            entity = Definition.class
    )

    public List<Definition> definitions;
}

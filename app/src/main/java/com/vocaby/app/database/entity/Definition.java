package com.vocaby.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "dictionary_definition",
        foreignKeys = {@ForeignKey(entity = Word.class,
                parentColumns = "id",
                childColumns = "word_id",
                onDelete = ForeignKey.CASCADE)
        })
public class Definition {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "word_id")
    private int wordId;

    @NonNull
    private String definition;
    @NonNull
    private String pos;
    private String sentence;

    public String getDefinition() {
        return this.definition;
    }

    public String getPos() {
        return this.pos;
    }

    public String getSentence() {
        return this.sentence;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
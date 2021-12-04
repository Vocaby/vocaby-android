package com.vocaby.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "dictionary_word",
        indices = {@Index("word")})
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String word;
    private String pronunciation;

    public Word() {
        word = "";
        pronunciation = "";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getWord() {
        return word;
    }

    public void setWord(@NonNull String word) {
        this.word = word;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getPronunciation() {
        return pronunciation;
    }
}
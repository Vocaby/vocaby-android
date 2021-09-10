package com.vocaby.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_added")
public class OfflineAddedSaves {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String word;

    public OfflineAddedSaves(String word) {
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}

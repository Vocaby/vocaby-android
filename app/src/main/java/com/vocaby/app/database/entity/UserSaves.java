package com.vocaby.app.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saves")
public class UserSaves {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private String word;

    public UserSaves(int userId, String word) {
        this.userId = userId;
        this.word = word;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

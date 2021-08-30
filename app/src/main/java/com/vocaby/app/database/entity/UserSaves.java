package com.vocaby.app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "saves", foreignKeys = {
        @ForeignKey(onDelete = ForeignKey.CASCADE,
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id")
}, indices = {@Index(value = {"user_id", "word"}, unique = true)})
public class UserSaves {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
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

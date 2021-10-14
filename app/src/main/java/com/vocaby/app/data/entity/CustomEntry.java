package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_user_entry")
public class CustomEntry {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_id")
    private int entryId;

    @ColumnInfo(name = "user_id")
    private int userId;

    private String entry;
    private String pronunciation;

    @ColumnInfo(name = "last_updated")
    private long date;

    public CustomEntry() {
        entryId = -1;
        userId = -1;
        date = -1;
    }

    @Ignore
    public CustomEntry(int userId, String entry, long date) {
        this.userId = userId;
        this.entry = entry;
        this.date = date;
    }

    @Ignore
    public CustomEntry(int entryId, int userId, String entry, long date) {
        this.entryId = entryId;
        this.userId = userId;
        this.entry = entry;
        this.date = date;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
}

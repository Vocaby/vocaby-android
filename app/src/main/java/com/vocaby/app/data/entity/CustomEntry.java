package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_user_entry",
        indices = {@Index("entry")})
public class CustomEntry {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_id")
    private int entryId;

    @ColumnInfo(name = "user_id")
    private int userId;

    private String entry;
    private String pronunciation;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    public CustomEntry() {
        entryId = -1;
        userId = -1;
        lastUpdated = -1;
    }

    @Ignore
    public CustomEntry(int userId, String entry, String pronunciation, long lastUpdated) {
        this.userId = userId;
        this.entry = entry;
        this.pronunciation = pronunciation;
        this.lastUpdated = lastUpdated;
    }

    @Ignore
    public CustomEntry(int entryId, int userId, String entry, String pronunciation, long lastUpdated) {
        this.entryId = entryId;
        this.userId = userId;
        this.entry = entry;
        this.pronunciation = pronunciation;
        this.lastUpdated = lastUpdated;
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

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
}

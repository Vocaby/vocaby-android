package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_entry_group", foreignKeys = {
        @ForeignKey(onDelete = ForeignKey.CASCADE,
                entity = CustomEntry.class,
                parentColumns = "custom_entry_id",
                childColumns = "custom_entry_id")},
        indices = {@Index("custom_entry_id")})
public class CustomEntryGroup {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_group_id")
    private int groupId;

    @ColumnInfo(name = "custom_entry_id")
    private int entryId;

    @ColumnInfo(name = "type")
    private String type;

    private int order;

    public CustomEntryGroup() {

    }

    @Ignore
    public CustomEntryGroup(int groupId) {
        this.groupId = groupId;
    }

    @Ignore
    public CustomEntryGroup(int groupId, int entryId, String type, int order) {
        this.groupId = groupId;
        this.entryId = entryId;
        this.type = type;
        this.order = order;
    }

    @Ignore
    public CustomEntryGroup(int entryId, String type, int order) {
        this.entryId = entryId;
        this.type = type;
        this.order = order;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}



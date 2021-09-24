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
                childColumns = "custom_entry_id"),
        @ForeignKey(entity = Type.class,
                parentColumns = "type_id",
                childColumns = "type_id")},
        indices = {@Index("custom_entry_id")})
public class CustomEntryGroup {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_group_id")
    private int groupId;

    @ColumnInfo(name = "custom_entry_id")
    private int entryId;

    @ColumnInfo(name = "type_id")
    private int type_id;

    private int order;

    public CustomEntryGroup() {

    }

    @Ignore
    public CustomEntryGroup(int entryId, int type_id, int order) {
        this.entryId = entryId;
        this.type_id = type_id;
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

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}



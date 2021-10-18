package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "entry_type")
public class Type {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "type_id")
    private int typeId;

    private String type;

    public Type(String type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

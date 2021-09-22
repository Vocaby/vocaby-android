package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "entry_type")
public class Type {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "type_id")
    private int id;

    private String type;

    public Type(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }
}

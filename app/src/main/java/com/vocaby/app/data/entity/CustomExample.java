package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_user_example", foreignKeys = {
        @ForeignKey(onDelete = ForeignKey.CASCADE,
                entity = CustomDefinition.class,
                parentColumns = "custom_definition_id",
                childColumns = "custom_definition_id")})
public class CustomExample {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_example_id")
    private int exampleId;

    @ColumnInfo(name = "custom_definition_id")
    private int definitionId;

    private String example;

    public CustomExample(int definitionId, String example) {
        this.definitionId = definitionId;
        this.example = example;
    }

    public int getExampleId() {
        return exampleId;
    }

    public void setExampleId(int exampleId) {
        this.exampleId = exampleId;
    }

    public int getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(int definitionId) {
        this.definitionId = definitionId;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    @Override
    public String toString() {
        return example;
    }
}

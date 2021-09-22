package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_user_definition", foreignKeys = {
        @ForeignKey(onDelete = ForeignKey.CASCADE,
                entity = CustomEntryGroup.class,
                parentColumns = "custom_entry_group_id",
                childColumns = "custom_entry_group_id")})
public class CustomDefinition {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_definition_id")
    private int definitionId;

    @ColumnInfo(name = "custom_entry_group_id")
    private int groupId;

    private String definition;
    private int order;

    public CustomDefinition(int groupId, String definition, int order) {
        this.groupId = groupId;
        this.definition = definition;
        this.order = order;
    }

    public int getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(int definitionId) {
        this.definitionId = definitionId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

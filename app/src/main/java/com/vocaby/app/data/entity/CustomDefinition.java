package com.vocaby.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_user_definition", foreignKeys = {
        @ForeignKey(onDelete = ForeignKey.CASCADE,
                entity = CustomEntryGroup.class,
                parentColumns = "custom_entry_group_id",
                childColumns = "custom_entry_group_id")},
        indices = @Index("custom_entry_group_id"))
public class CustomDefinition implements Comparable<CustomDefinition> {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_definition_id")
    private int definitionId;

    @ColumnInfo(name = "custom_entry_group_id")
    private int groupId;

    private String definition;
    private String example;
    private int order;

    @Ignore
    public CustomDefinition(int groupId, String definition, String example, int order) {
        this.groupId = groupId;
        this.definition = definition;
        this.order = order;
        this.example = example;
    }

    public CustomDefinition(int definitionId, int groupId, String definition, String example, int order) {
        this.definitionId = definitionId;
        this.groupId = groupId;
        this.definition = definition;
        this.order = order;
        this.example = example;
    }

    @Ignore
    public CustomDefinition(int groupId, String definition, int order) {
        this.groupId = groupId;
        this.definition = definition;
        this.order = order;
        example = "";
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

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(CustomDefinition customDefinition) {
        return Integer.compare(order, customDefinition.getOrder());
    }
}

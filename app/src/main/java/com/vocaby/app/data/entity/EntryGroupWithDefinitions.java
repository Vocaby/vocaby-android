package com.vocaby.app.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class EntryGroupWithDefinitions implements Comparable<EntryGroupWithDefinitions> {
    @Embedded
    public CustomEntryGroup entryGroup;

    @Relation(
            parentColumn = "custom_entry_group_id",
            entityColumn = "custom_entry_group_id",
            entity = CustomDefinition.class
    )
    public List<CustomDefinition> definitions;

    @Override
    public int compareTo(EntryGroupWithDefinitions e) {
        return Integer.compare(entryGroup.getOrder(), e.entryGroup.getOrder());
    }
}

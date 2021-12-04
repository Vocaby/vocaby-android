package com.vocaby.app.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class EntryWithData {
    @Embedded
    public CustomEntry customEntry;

    @Relation(
            parentColumn = "custom_entry_id",
            entityColumn = "custom_entry_id",
            entity = CustomEntryGroup.class
    )
    public List<EntryGroupWithDefinitions> groups;
}

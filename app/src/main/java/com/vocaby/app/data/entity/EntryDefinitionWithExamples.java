package com.vocaby.app.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class EntryDefinitionWithExamples implements Comparable<EntryDefinitionWithExamples> {
    @Embedded
    public CustomDefinition customDefinition;

    @Relation(
            parentColumn = "custom_definition_id",
            entityColumn = "custom_definition_id",
            entity = CustomExample.class
    )
    public List<CustomExample> examples;

    @Override
    public int compareTo(EntryDefinitionWithExamples e) {
        return Integer.compare(customDefinition.getOrder(), e.customDefinition.getOrder());
    }
}

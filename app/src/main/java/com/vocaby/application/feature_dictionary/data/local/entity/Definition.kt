package com.vocaby.application.feature_dictionary.data.local.entity

import androidx.room.*

@Entity(
    tableName = "dictionary_definition",
    foreignKeys = [ForeignKey(
        entity = Entry::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["entry_id"])]
)
data class Definition(
    @ColumnInfo(name = "entry_id")
    val entryId: Int,
    val definition: String,
    val example: String?,
    @ColumnInfo(name = "definition_type")
    val type: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
)
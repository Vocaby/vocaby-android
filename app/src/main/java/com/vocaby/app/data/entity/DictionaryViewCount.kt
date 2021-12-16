package com.vocaby.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dictionary_view_count",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"]
    ), ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = Word::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"]
    )],
)
data class DictionaryViewCount(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dictionary_view_id")
    val viewId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "entry_id") val entryId: Int,
    @ColumnInfo(name = "date_visited") val dateVisited: String
)
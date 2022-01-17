package com.vocaby.application.feature_user.data.local.entity

import androidx.room.*
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry

@Entity(
    tableName = "custom_dictionary_view_count",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"]
    ), ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = CustomEntry::class,
        parentColumns = ["custom_entry_id"],
        childColumns = ["custom_entry_id"]
    )],
    indices = [Index("custom_entry_id"), Index("user_id")]
)
data class CustomDictionaryViewCount(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dictionary_view_id")
    val viewId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "custom_entry_id") val entryId: Int,
    @ColumnInfo(name = "date_visited") val dateVisited: String
)
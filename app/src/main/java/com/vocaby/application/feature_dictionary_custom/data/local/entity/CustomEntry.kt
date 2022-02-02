package com.vocaby.application.feature_dictionary_custom.data.local.entity

import androidx.room.*
import com.vocaby.application.feature_profile.data.local.entity.User
import java.util.*

@Entity(
    tableName = "custom_user_entry",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = arrayOf("user_id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [Index(value = ["entry", "custom_entry_id"], unique = true)]
)
data class CustomEntry(
    @ColumnInfo(name = "user_id")
    var userId: Int,
    var entry: String,
    var pronunciation: String?,
    @ColumnInfo(name = "last_updated")
    var lastUpdated: Date = Date(),
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "custom_entry_id")
    var entryId: Int = 0,
)
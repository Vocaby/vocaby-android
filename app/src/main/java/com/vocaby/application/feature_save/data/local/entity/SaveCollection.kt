package com.vocaby.application.feature_save.data.local.entity

import androidx.room.*
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_profile.data.local.entity.User
import java.util.*

@Entity(
    tableName = "save_collection",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = arrayOf("user_id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [
        Index(value=["user_id", "collection_name"], unique = true)
    ]
)
data class SaveCollection(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "collection_name")
    val collectionName: String,
    @ColumnInfo(name="last_updated")
    val lastUpdated: String = Formatter.formatDateToString(Date().time),
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "collection_id", index = true)
    val id: Int = 0,
)

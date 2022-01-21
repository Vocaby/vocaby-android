package com.vocaby.application.feature_save.data.local.entity

import androidx.room.*
import com.vocaby.application.feature_profile.data.local.entity.User

@Entity(
    tableName = "save_collection",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = arrayOf("user_id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [Index(value=["collection_id", "user_id"])]
)
data class SaveCollection(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "collection_id")
    val id: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "collection_name")
    val collectionName: String,
    @ColumnInfo(name="last_updated")
    val lastUpdated: String
)

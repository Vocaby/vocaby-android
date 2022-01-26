package com.vocaby.application.feature_save.data.local.entity

import androidx.room.*
import com.vocaby.application.core.util.Formatter
import java.util.*

@Entity(
    tableName = "save_collection_item",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = UserSave::class,
        parentColumns = arrayOf("save_id"),
        childColumns = arrayOf("save_id")
    ), ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = SaveCollection::class,
        parentColumns = arrayOf("collection_id"),
        childColumns = arrayOf("collection_id")
    )],
    indices = [Index(value=["save_id", "collection_id"], unique=true)]
)
data class SaveCollectionItem(
    @ColumnInfo(name = "save_id")
    val saveId: Int,
    @ColumnInfo(name = "collection_id")
    val collectionId: Int,
    @ColumnInfo(name="last_added")
    val lastAdded: String = Formatter.formatDateToString(Date().time),
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "collection_item_id", index = true)
    val id: Int = 0,
)

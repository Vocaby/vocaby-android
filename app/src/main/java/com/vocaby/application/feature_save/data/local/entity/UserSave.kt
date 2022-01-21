package com.vocaby.application.feature_save.data.local.entity

import androidx.room.*
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry
import com.vocaby.application.feature_profile.data.local.entity.User

@Entity(
    tableName = "saves",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = arrayOf("user_id"),
        childColumns = arrayOf("user_id")
    ), ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = Word::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("entry_id")
    ), ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = CustomEntry::class,
        parentColumns = arrayOf("custom_entry_id"),
        childColumns = arrayOf("custom_entry_id")
    )],
    indices = [Index(value = ["user_id", "entry_id", "custom_entry_id"], unique = true)]
)
class UserSave(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "entry_id")
    val entryId: Int? = null,
    @ColumnInfo(name = "custom_entry_id")
    val customEntryId: Int? = null,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "save_id")
    var id: Int = 0
)
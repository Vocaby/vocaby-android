package com.vocaby.application.feature_save.data.local.entity

import androidx.room.*
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry
import com.vocaby.application.feature_profile.data.local.entity.User
import java.util.*

@Entity(
    tableName = "saves",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = arrayOf("user_id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [
        Index(value=["user_id", "entry"], unique = true)
    ]
)
class UserSave(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    val entry: String,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "save_id")
    var id: Int = 0,
    @ColumnInfo(name="last_saved")
    val lastSaved: String = Formatter.formatDateToString(Date().time)
)
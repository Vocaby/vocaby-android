package com.vocaby.application.feature_user.data.local.entity

import androidx.room.*

@Entity(
    tableName = "saves",
    foreignKeys = [ForeignKey(
        onDelete = ForeignKey.CASCADE,
        entity = User::class,
        parentColumns = arrayOf("user_id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [Index(value = ["user_id", "entry"], unique = true)]
)
class UserSave(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    var entry: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
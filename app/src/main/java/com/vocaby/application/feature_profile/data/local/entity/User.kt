package com.vocaby.application.feature_profile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocaby_user"
)
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id", index = true)
    var userId: Int = 0,
    var email: String = "",
    var firstName: String = "Guest",
    var lastName: String = ""
)
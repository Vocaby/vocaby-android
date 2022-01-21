package com.vocaby.application.feature_profile.data.local.entity

import androidx.room.*

@Entity(
    tableName = "vocaby_user",
    indices = [Index(value=["user_id"])]
)
class User {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    var userId = 0
    var email: String = ""
    var firstName: String = "Guest"
    var lastName: String = ""

    constructor()

    @Ignore
    constructor(email: String, firstName: String, lastName: String) {
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
    }
}
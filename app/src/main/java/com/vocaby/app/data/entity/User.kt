package com.vocaby.app.data.entity

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

@Entity(tableName = "vocaby_user")
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
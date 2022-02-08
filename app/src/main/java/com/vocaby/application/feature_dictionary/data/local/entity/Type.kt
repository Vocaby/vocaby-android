package com.vocaby.application.feature_dictionary.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "entry_type",
    indices = [Index(value = ["type"], unique = true)]
)
@Parcelize
data class Type(
    var type: String,
    var order: Int,
    var userDefined: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "type_id", index = true)
    var typeId: Int = 0
): Parcelable
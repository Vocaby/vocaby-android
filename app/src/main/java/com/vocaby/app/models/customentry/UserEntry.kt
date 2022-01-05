package com.vocaby.app.models.customentry

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserEntry(val entry: String, @ColumnInfo(name = "last_updated")val lastUpdated: Long): Parcelable

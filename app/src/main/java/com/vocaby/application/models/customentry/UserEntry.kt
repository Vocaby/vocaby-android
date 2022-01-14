package com.vocaby.application.models.customentry

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class UserEntry(val entry: String, @ColumnInfo(name = "last_updated")var lastUpdated: Date): Parcelable

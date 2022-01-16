package com.vocaby.application.core.data

import androidx.room.TypeConverter
import com.vocaby.application.core.util.Formatter
import java.util.*

class Converters {
    @TypeConverter
    fun fromStringToDate(date: String?): Date {
        return Formatter.formatStringToDate(date ?: "2022-01-01")
    }

    @TypeConverter
    fun fromDateToString(date: Date): String {
        return Formatter.formatDateToString(date.time)
    }
}
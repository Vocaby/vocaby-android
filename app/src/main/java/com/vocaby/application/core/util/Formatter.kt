package com.vocaby.application.core.util

import com.vocaby.application.core.Constants.SPECIAL_CHARACTERS
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Formatter {
    fun addDatePrefix(name: String, date: Date = Date()): String {
        val prefix = SimpleDateFormat("MMddyyyy", Locale.getDefault()).format(date)
        return "${prefix}_$name"
    }

    fun formatDateToString(milli: Long, forDisplay:Boolean = false, showDay:Boolean = false, precise: Boolean = true): String {
        return if (forDisplay) {
            if (showDay) {
                SimpleDateFormat("EEE MM.dd.yyyy", Locale.getDefault()).format(milli)
            } else {
                SimpleDateFormat("MM.dd.yyyy", Locale.getDefault()).format(milli)
            }
        } else {
            if (precise) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(milli)
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(milli)
            }
        }

    }

    fun formatStringToDate(date: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)!!
        } catch (e: ParseException) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
        }
    }

    fun cleanText(text: String, lowercase: Boolean = true): String {
        val sanitized = text.trim { it <= ' ' }.replace(SPECIAL_CHARACTERS.toRegex(), "").replace("`","'")

        return if (lowercase) sanitized.lowercase()
        else sanitized
    }

    fun firstLetterUpperCase(text: String): String {
        var newString: String = text
        if (text.length == 1) newString =  text.uppercase(Locale.getDefault())
        else if (text.length > 1) {
            newString = text.split(" ").joinToString(" ") { it -> it.replaceFirstChar { it.uppercase() } }
        }

        return newString
    }

    fun cleanNumber(num: Int, singularSuffix: String = "", pluralSuffix: String = ""): String {
        return if (num < 2) {
            var formatted = NumberFormat.getNumberInstance(Locale.US).format(num.toLong())
            if (singularSuffix.isNotEmpty()) formatted += " $singularSuffix"
            formatted
        } else {
            var formatted = NumberFormat.getNumberInstance(Locale.US).format(num.toLong())
            if (pluralSuffix.isNotEmpty()) formatted += " $pluralSuffix"
            formatted
        }
    }
}
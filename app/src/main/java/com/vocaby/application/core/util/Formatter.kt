package com.vocaby.application.core.util

import com.vocaby.application.core.Constants
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Formatter {
    fun dateIsValid(date: String): Boolean {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)
            true
        } catch (e: ParseException) {
            return try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                true
            } catch (e: ParseException) {
                false
            }
        }
    }

    fun formatDateToString(milli: Long, forDisplay:Boolean = false, showDay:Boolean = false, precise: Boolean = true): String {
        if (forDisplay) {
            if (showDay) {
                return SimpleDateFormat("EEE MM.dd.yyyy", Locale.getDefault()).format(milli)
            } else {
                return SimpleDateFormat("MM.dd.yyyy", Locale.getDefault()).format(milli)
            }
        } else {
            if (precise) {
                return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(milli)
            } else {
                return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(milli)
            }
        }

    }

    fun formatStringToDate(date: String): Date {
        try {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)!!
        } catch (e: ParseException) {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
        }
    }

    fun cleanText(text: String): String {
        return text.trim { it <= ' ' }.replace("[\\\\{}]".toRegex(), "").lowercase()
    }

    fun containsSpecialCharacter(text: String): Boolean {
        return text.contains("[\\\\{}]".toRegex())
    }

    fun firstLetterUpperOnly(text: String): String {
        var newString: String = text
        if (text.length == 1) newString =  text.uppercase(Locale.getDefault())
        else if (text.length > 1) newString = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1).lowercase()

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

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateEntry(entry: String): Boolean {
        return !containsSpecialCharacter(entry) && entry.length <= Constants.ENTRY_MAX_LENGTH
    }
}
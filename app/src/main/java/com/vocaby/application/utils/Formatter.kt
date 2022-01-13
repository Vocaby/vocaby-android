package com.vocaby.application.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatter {
    fun dateIsValid(date: String): Boolean {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun formatDateToString(milli: Long, forDisplay:Boolean = false): String {
        if (forDisplay) {
            return SimpleDateFormat("EEE MM.dd.yyyy", Locale.getDefault()).format(milli)
        } else {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(milli)
        }

    }

    fun formatStringToDate(date: String): Date? {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
    }

    fun formatDateStringForDisplay(date: String, showDay:Boolean = false): String {
        val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)

        return if (showDay) {
            SimpleDateFormat("EEE MM.dd.yyyy", Locale.getDefault()).format(formatted!!)
        } else {
            SimpleDateFormat("MM.dd.yyyy", Locale.getDefault()).format(formatted!!)
        }
    }

    fun cleanText(text: String): String {
        return text.trim { it <= ' ' }.replace("[^\\p{L}0-9!$*()\\[\\]`/+?=~:;'‘̇̄’̧.,_ -]".toRegex(), "").lowercase()
    }

    fun containsSpecialCharacter(text: String): Boolean {
        return text.contains("[^\\p{L}0-9!$*()\\[\\]`/+?=~:;'‘̇̄’̧.,_ -]".toRegex())
    }

    fun firstLetterUpperOnly(text: String): String {
        var newString: String = text
        if (text.length == 1) newString =  text.uppercase(Locale.getDefault())
        else if (text.length > 1) newString = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1).lowercase()

        return newString
    }

    fun cleanNumber(num: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(num.toLong())
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
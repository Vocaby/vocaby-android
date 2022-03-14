package com.vocaby.application.core.util

import com.vocaby.application.core.Constants
import com.vocaby.application.feature_dictionary_custom.common.Constants.ENTRY_MAX_LENGTH
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Validator {
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

    fun containsSpecialCharacter(text: String): Boolean {
        return text.contains(Constants.SPECIAL_CHARACTERS.toRegex())
    }

    fun emailIsValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun entryIsValid(entry: String): Boolean {
        return !containsSpecialCharacter(entry) && entry.length <= ENTRY_MAX_LENGTH
    }
}
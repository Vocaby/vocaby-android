package com.vocaby.app.utils

import java.text.NumberFormat
import java.util.*

object StringFormatter {
    fun cleanText(text: String): String {
        return text.trim { it <= ' ' }.replace("[^\\p{L}0-9!?'.,_ -]".toRegex(), "").lowercase()
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
}
package com.vocaby.application.utils

import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.roundToInt

class AxisValueFormatter(private val values: List<String>, val maxLength: Int): ValueFormatter() {
    private val count = values.size

    override fun getFormattedValue(value: Float): String {
        val index = value.roundToInt()
        return if (index < 0 || index >= count || index != value.toInt()) ""
        else {
            var value = values[index]
            if (value.length > maxLength) {
                value = value.substring(0, maxLength) + "\u2026"
            }

            return value
        }
    }
}
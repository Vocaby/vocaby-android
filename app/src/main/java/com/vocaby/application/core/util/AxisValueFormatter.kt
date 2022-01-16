package com.vocaby.application.core.util

import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.roundToInt

class AxisValueFormatter(private val values: List<String>, val maxLength: Int): ValueFormatter() {
    private val count = values.size

    override fun getFormattedValue(value: Float): String {
        val index = value.roundToInt()
        return if (index < 0 || index >= count || index != value.toInt()) ""
        else {
            var selected = values[index]
            if (selected.length > maxLength) {
                selected = selected.substring(0, maxLength) + "\u2026"
            }

            return selected
        }
    }
}
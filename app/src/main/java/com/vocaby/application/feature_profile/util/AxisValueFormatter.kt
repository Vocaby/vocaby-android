package com.vocaby.application.feature_profile.util

import com.github.mikephil.charting.formatter.ValueFormatter
import com.vocaby.application.feature_profile.common.Constants
import kotlin.math.roundToInt

class AxisValueFormatter(
    private val values: List<String>,
    private val minLength: Int = Constants.PROFILE_CHART_LABEL_MIN_LENGTH,
    private val maxLength: Int = Constants.PROFILE_CHART_LABEL_MAX_LENGTH
): ValueFormatter() {
    private val count = values.size

    override fun getFormattedValue(value: Float): String {
        val index = value.roundToInt()
        return if (index < 0 || index >= count || index != value.toInt()) ""
        else {
            val extraSpace =
                ((maxLength - minLength) / (Constants.MAX_BARS - Constants.MIN_BARS)) * (Constants.MAX_BARS - count)
            val allowedLength = minLength + extraSpace
            var selected = values[index]
            if (selected.length > allowedLength) {
                selected = selected.substring(0, allowedLength) + "-"
            }

            return selected
        }
    }
}
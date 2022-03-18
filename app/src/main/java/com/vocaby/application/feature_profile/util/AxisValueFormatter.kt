package com.vocaby.application.feature_profile.util

import android.graphics.Paint
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vocaby.application.feature_profile.common.Constants
import kotlin.math.roundToInt

class AxisValueFormatter(
    private val values: List<String>,
    private val width: Double,
    private val size: Float,
    private val minLength: Int = Constants.PROFILE_CHART_LABEL_MIN_LENGTH,
    private val maxLength: Int = Constants.PROFILE_CHART_LABEL_MAX_LENGTH
): ValueFormatter() {
    private val calculatedMaxCache: MutableMap<Float, Int> = mutableMapOf()
    private val count = values.size

    override fun getFormattedValue(value: Float): String {
        val index = value.roundToInt()
        return if (index < 0 || index >= count || index != value.toInt()) ""
        else {
            var selected = values[index]

            val extraSpace =
                ((maxLength - minLength) / (Constants.MAX_BARS - Constants.MIN_BARS)) * (Constants.MAX_BARS - count)

            val allowedLength = if (calculatedMaxCache.containsKey(value)) {
                calculatedMaxCache[value]!!
            } else {
                val paint = Paint().apply { textSize = size }
                val characterAverageWidth = paint.measureText(values[index]) / values[index].length
                val max = (width / characterAverageWidth).toInt()

                if (max > 3) {
                    calculatedMaxCache[value] = max
                    max
                } else {
                    minLength + extraSpace
                }
            }

            if (selected.length > allowedLength) {
                selected = selected.substring(0, allowedLength) + ".."
            }

            return selected
        }
    }
}
package com.vocaby.vocabywidgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.ChipGroup

class ChipGroup: ChipGroup {
    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressLint("RestrictedApi")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val maxWidth =
            if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) width else Int.MAX_VALUE

        var childLeft = paddingLeft
        var childTop = paddingTop
        var childBottom = childTop
        var childRight: Int
        var maxChildRight = 0
        val maxRight = maxWidth - paddingRight
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }

            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            val lp: ViewGroup.LayoutParams? = child.layoutParams
            var leftMargin = 0
            var rightMargin = 0

            if (lp is MarginLayoutParams) {
                leftMargin += lp.leftMargin
                rightMargin += lp.rightMargin
            }

            childRight = childLeft + leftMargin + child.measuredWidth

            // If the current child's right bound exceeds Flowlayout's max right bound and flowlayout is
            // not confined to a single line, move this child to the next line and reset its left bound to
            // flowlayout's left bound.
            if (childRight > maxRight && !isSingleLine) {
                childLeft = paddingLeft
                childTop = childBottom + lineSpacing
            }

            childBottom = childTop + child.measuredHeight

            // Updates Flowlayout's max right bound if current child's right bound exceeds it.
//            if (childRight > maxChildRight) {
//                maxChildRight = childRight
//            }
            childLeft += leftMargin + rightMargin + child.measuredWidth + itemSpacing

            // For all preceding children, the child's right margin is taken into account in the next
            // child's left bound (childLeft). However, childLeft is ignored after the last child so the
            // last child's right margin needs to be explicitly added to Flowlayout's max right bound.
            if (i == childCount - 1) {
                maxChildRight += rightMargin
            }
        }

        maxChildRight += paddingRight
        childBottom += paddingBottom

        val finalWidth = getMeasuredDimension(width, widthMode, maxChildRight)
        val finalHeight = getMeasuredDimension(height, heightMode, childBottom)
        setMeasuredDimension(finalWidth, finalHeight)
    }

    private fun getMeasuredDimension(size: Int, mode: Int, childrenEdge: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> childrenEdge.coerceAtMost(size)
            else -> childrenEdge
        }
    }
}
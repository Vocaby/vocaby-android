package com.vocaby.vocabywidgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat

class DescriptiveButtonView: LinearLayoutCompat {
    private var mOnClickListener: OnClickListener? = null
    private var attributes: TypedArray? = null
    private val headerView: TextView
    private val descriptionView: TextView

    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val view = inflate(context, R.layout.descriptive_button_layout, this)
        headerView = view.findViewById(R.id.header)
        descriptionView = view.findViewById(R.id.description)
        bringToFront()
        attributes = context.obtainStyledAttributes(attrs, R.styleable.DescriptiveButtonView)
        isEnabled = context.theme.resolveAttribute(android.R.attr.enabled, TypedValue(), true)
        headerView.text = attributes?.getString(R.styleable.DescriptiveButtonView_headerText)
        descriptionView.text = attributes?.getString(R.styleable.DescriptiveButtonView_descriptionText)
        val headerColors = attributes?.getColorStateList(R.styleable.DescriptiveButtonView_headerColor)
        val descriptionColors = attributes?.getColorStateList(R.styleable.DescriptiveButtonView_descriptionColor)

        headerColors?.let {
            headerView.setTextColor(it)
        }

        descriptionColors?.let {
            descriptionView.setTextColor(descriptionColors)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        headerView.isEnabled = enabled
        descriptionView.isEnabled = enabled
    }

    fun setDescription(description: String) {
        descriptionView.text = description
    }

    fun getDescription(): String {
        return descriptionView.text.toString()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        mOnClickListener = l
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }
}
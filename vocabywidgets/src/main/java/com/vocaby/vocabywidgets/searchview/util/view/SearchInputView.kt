package com.vocaby.vocabywidgets.searchview.util.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View.OnKeyListener
import androidx.appcompat.widget.AppCompatEditText

class SearchInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var mSearchKeyListener: OnKeyboardSearchKeyClickListener? = null
    private var mOnKeyboardDismissedListener: OnKeyboardDismissedListener? = null
    private val mOnKeyListener = OnKeyListener { _, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_ENTER && mSearchKeyListener != null) {
            mSearchKeyListener!!.onSearchKeyClicked()
            return@OnKeyListener true
        }
        false
    }

    init {
        setOnKeyListener(mOnKeyListener)
    }

    override fun onKeyPreIme(keyCode: Int, ev: KeyEvent): Boolean {
        if (ev.keyCode == KeyEvent.KEYCODE_BACK && mOnKeyboardDismissedListener != null) {
            mOnKeyboardDismissedListener!!.onKeyboardDismissed()
        }
        return super.onKeyPreIme(keyCode, ev)
    }

    fun setOnKeyboardDismissedListener(onKeyboardDismissedListener: OnKeyboardDismissedListener?) {
        mOnKeyboardDismissedListener = onKeyboardDismissedListener
    }

    fun setOnSearchKeyListener(searchKeyListener: OnKeyboardSearchKeyClickListener?) {
        mSearchKeyListener = searchKeyListener
    }

    interface OnKeyboardDismissedListener {
        fun onKeyboardDismissed()
    }

    interface OnKeyboardSearchKeyClickListener {
        fun onSearchKeyClicked()
    }
}
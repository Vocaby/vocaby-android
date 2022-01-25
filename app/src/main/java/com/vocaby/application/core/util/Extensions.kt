package com.vocaby.application.core.util

import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun Snackbar.config(context: Context, drawableId: Int) {
    animationMode = Snackbar.ANIMATION_MODE_SLIDE
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(0, 0, 0, 0)
    this.view.layoutParams = params
    this.view.setPadding(
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt(),
        0,
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt(),
        0
    )
    this.view.background = ContextCompat.getDrawable(context, drawableId)
}
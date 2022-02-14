package com.vocaby.vocabywidgets.searchview.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

object Util {
    fun showSoftKeyboard(context: Context, editText: EditText?) {
        Handler(Looper.getMainLooper()).postDelayed({
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
        }, 100)
    }

    fun closeSoftKeyboard(activity: Activity) {
        val currentFocusView = activity.currentFocus
        if (currentFocusView != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

    fun dpToPx(dp: Int): Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * metrics.density).toInt()
    }


    /**
     * Gets a reference to a given drawable and prepares it for use with tinting through.
     *
     * @param resId the resource id for the given drawable
     * @return a wrapped drawable ready fo use
     * with [androidx.core.graphics.drawable.DrawableCompat]'s tinting methods
     * @throws Resources.NotFoundException
     */
    @Throws(NotFoundException::class)
    fun getWrappedDrawable(context: Context, @DrawableRes resId: Int): Drawable {
        return DrawableCompat.wrap(
            ResourcesCompat.getDrawable(
                context.resources,
                resId, null
            )!!
        )
    }

    @Throws(NotFoundException::class)
    fun getColor(context: Context?, @ColorRes resId: Int): Int {
        return ContextCompat.getColor(context!!, resId)
    }

    fun removeGlobalLayoutObserver(view: View, layoutListener: OnGlobalLayoutListener?) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }

    fun getHostActivity(context: Context?): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }

            ctx = ctx.baseContext
        }

        return null
    }
}

inline fun <T: View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}
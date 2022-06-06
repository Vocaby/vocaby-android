package com.vocaby.application.core.util

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.vocaby.application.R

object UiHelper {
    fun showSnackBar(
        coordinatorLayout: CoordinatorLayout,
        context: Context,
        message: String,
        showAction: Boolean = false,
        actionMessageId: Int? = null,
        action: () -> Unit = {}
    ) {
        val snackbar = Snackbar.make(
            coordinatorLayout,
            message,
            Snackbar.LENGTH_LONG
        )

        if (showAction && actionMessageId != null) snackbar.setAction(actionMessageId) {
            action()
        }

        snackbar.config(context, R.drawable.snackbar_background)
        snackbar.show()
    }
}
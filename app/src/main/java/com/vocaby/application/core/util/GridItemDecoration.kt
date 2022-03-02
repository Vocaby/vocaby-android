package com.vocaby.application.core.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class GridItemDecoration(private val margin: Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
        layoutParams.bottomMargin = margin * 2

        if (position % 2 == 0) {
            layoutParams.rightMargin = margin
            layoutParams.leftMargin = 0
        } else {
            layoutParams.rightMargin = 0
            layoutParams.leftMargin = margin
        }

        view.layoutParams = layoutParams
    }
}
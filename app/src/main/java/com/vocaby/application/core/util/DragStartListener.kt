package com.vocaby.application.core.util

import androidx.recyclerview.widget.RecyclerView

interface DragStartListener {
    fun onDragStart(viewHolder: RecyclerView.ViewHolder)
}
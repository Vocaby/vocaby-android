package com.vocaby.app.adapters

import androidx.recyclerview.widget.RecyclerView

interface DragStartListener {
    fun onDragStart(viewHolder: RecyclerView.ViewHolder)
}
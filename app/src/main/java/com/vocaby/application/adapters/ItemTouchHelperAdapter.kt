package com.vocaby.application.adapters

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}
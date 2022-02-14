package com.vocaby.vocabywidgets.searchview.util.adapter

import android.view.GestureDetector
import android.view.MotionEvent

abstract class GestureDetectorListenerAdapter : GestureDetector.OnGestureListener {
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    companion object {
        private const val TAG = "GestureDetectorListenerAdapter"
    }
}
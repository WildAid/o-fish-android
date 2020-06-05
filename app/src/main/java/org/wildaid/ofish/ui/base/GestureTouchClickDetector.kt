package org.wildaid.ofish.ui.base

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class GestureTouchClickDetector(context: Context) :
    GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?) = true
        override fun onDoubleTap(e: MotionEvent?) = true
        override fun onSingleTapConfirmed(e: MotionEvent?) = true
        override fun onDoubleTapEvent(e: MotionEvent?) = true
    })
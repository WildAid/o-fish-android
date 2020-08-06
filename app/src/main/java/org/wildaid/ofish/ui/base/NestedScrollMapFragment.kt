package org.wildaid.ofish.ui.base

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import com.google.android.gms.maps.SupportMapFragment

class NestedScrollMapFragment : SupportMapFragment() {
    private var parentScrollView: NestedScrollView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout: View? = super.onCreateView(inflater, parent, savedInstanceState)
        val frameLayout = TouchableWrapper(requireContext())
        frameLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))
        (layout as ViewGroup?)!!.addView(
            frameLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        return layout
    }

    fun attachParentScroll(nestedScroll: NestedScrollView?) {
        this.parentScrollView = nestedScroll
    }

    inner class TouchableWrapper @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
    ) : FrameLayout(context, attrs, defStyle) {
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> parentScrollView?.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> parentScrollView?.requestDisallowInterceptTouchEvent(true)
            }
            return super.dispatchTouchEvent(event)
        }
    }
}
package org.wildaid.ofish.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import org.wildaid.ofish.util.hideKeyboard

class HideKeyboardRecyclerSpinner @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatSpinner(context, attrs, defStyleAttr) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboard()
        return super.onTouchEvent(ev)
    }
}
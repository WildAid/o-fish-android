package org.wildaid.ofish.ui.base

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor

class SafetyColorTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyle) {

    fun setSafetyColor(safetyColor: SafetyColor, @DimenRes radiusRes: Int) {
        when (safetyColor) {
            SafetyColor.Red -> {
                text = SafetyColor.Red.name
                setTextColor(context.getColor(R.color.red_text_2))
                background = createGradientDrawable(R.color.red_background, radiusRes)
            }
            SafetyColor.Green -> {
                text = SafetyColor.Green.name
                setTextColor(context.getColor(R.color.green_text))
                background = createGradientDrawable(R.color.green_background, radiusRes)
            }
            SafetyColor.Amber -> {
                text = SafetyColor.Amber.name
                setTextColor(context.getColor(R.color.amber_text))
                background = createGradientDrawable(R.color.amber_background, radiusRes)
            }
        }
    }

    private fun createGradientDrawable(@ColorRes colorRes: Int, @DimenRes radiusRes: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = resources.getDimension(radiusRes)
        gd.setColor(resources.getColor(colorRes, null))
        return gd
    }
}
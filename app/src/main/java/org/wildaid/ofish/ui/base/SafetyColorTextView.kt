package org.wildaid.ofish.ui.base

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatDelegate
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor

class SafetyColorTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyle) {

    fun setSafetyColor(safetyColor: SafetyColor, @DimenRes radiusRes: Int) {
        val isDarkModeEnabled = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        if (!isDarkModeEnabled) {
            setSafetyColorForLightMode(safetyColor, radiusRes)
        } else {
            setSafetyColorForDarkMode(safetyColor, radiusRes)
        }
    }

    private fun setSafetyColorForLightMode(safetyColor: SafetyColor, @DimenRes radiusRes: Int) {
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

        setTypeface(null, Typeface.BOLD)
    }

    private fun setSafetyColorForDarkMode(safetyColor: SafetyColor, @DimenRes radiusRes: Int) {
        when (safetyColor) {
            SafetyColor.Red -> {
                text = SafetyColor.Red.name
                setTextColor(context.getColor(R.color.red_night))
                background = createDarkModeGradientDrawable(R.color.red_night, radiusRes)
            }
            SafetyColor.Green -> {
                text = SafetyColor.Green.name
                setTextColor(context.getColor(R.color.green_night))
                background = createDarkModeGradientDrawable(R.color.green_night, radiusRes)
            }
            SafetyColor.Amber -> {
                text = SafetyColor.Amber.name
                setTextColor(context.getColor(R.color.amber_night))
                background = createDarkModeGradientDrawable(R.color.amber_night, radiusRes)
            }
        }

        setTypeface(null, Typeface.NORMAL)
    }

    private fun createGradientDrawable(
        @ColorRes colorRes: Int,
        @DimenRes radiusRes: Int
    ): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = resources.getDimension(radiusRes)
        gd.setColor(resources.getColor(colorRes, null))
        return gd
    }

    private fun createDarkModeGradientDrawable(
        @ColorRes colorRes: Int,
        @DimenRes radiusRes: Int
    ): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.setStroke(4, resources.getColor(colorRes, null))
        gd.cornerRadius = resources.getDimension(radiusRes)
        gd.setColor(resources.getColor(R.color.black_1, null))
        return gd
    }
}
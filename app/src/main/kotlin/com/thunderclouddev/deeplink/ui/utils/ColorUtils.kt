package com.thunderclouddev.deeplink.ui.utils

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.annotation.ColorRes
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.ImageView

/**
 * Created by https://gist.github.com/martintreurnicht/f6bbb20a43211bc2060e
 */
object ColorUtils {
    fun lighten(@ColorRes color: Int, fraction: Double): Int {
        var red = Color.red(color)
        var green = Color.green(color)
        var blue = Color.blue(color)
        red = lightenColor(red, fraction)
        green = lightenColor(green, fraction)
        blue = lightenColor(blue, fraction)
        val alpha = Color.alpha(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun darken(@ColorRes color: Int, fraction: Double): Int {
        var red = Color.red(color)
        var green = Color.green(color)
        var blue = Color.blue(color)
        red = darkenColor(red, fraction)
        green = darkenColor(green, fraction)
        blue = darkenColor(blue, fraction)
        val alpha = Color.alpha(color)

        return Color.argb(alpha, red, green, blue)
    }

    private fun darkenColor(color: Int, fraction: Double): Int {
        return Math.max(color - color * fraction, 0.0).toInt()
    }

    private fun lightenColor(color: Int, fraction: Double): Int {
        return Math.min(color + color * fraction, 255.0).toInt()
    }

    fun recolor(view: ImageView, color: Int, mode: PorterDuff.Mode) {
        val wrappedDrawable = DrawableCompat.wrap(view.drawable)
        DrawableCompat.setTint(wrappedDrawable, color)
        DrawableCompat.setTintMode(wrappedDrawable, mode)

        view.setImageDrawable(wrappedDrawable)
    }
}

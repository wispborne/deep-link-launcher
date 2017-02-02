package com.thunderclouddev.deeplink.utils

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * Uses compatibility library to create a tinted a drawable. Supports all important versions of Android.
 *
 * @param color    Color to tint to, *not* R.color.whatever, must be resolved
 * @param mode     Mode of tinting to use
 *
 * @return A tinted Drawable. Wraps the Drawable in a new class - instanceof will NOT match the old type
 */
fun Drawable?.tint(@ColorInt color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): Drawable? {
    if (this == null) {
        return null
    }

    val wrappedDrawable = DrawableCompat.wrap(this)
    DrawableCompat.setTint(wrappedDrawable, color)
    DrawableCompat.setTintMode(wrappedDrawable, mode)
    return wrappedDrawable
}

/**
 * Uses compatibility library to create a tinted a drawable. Supports all important versions of Android.
 * @param color    Color to tint to
 * @param mode     Mode of tinting to use
 *
 * @return A tinted Drawable. Wraps the Drawable in a new class - instanceof will NOT match the old type
 */
fun Drawable?.tint(color: ColorStateList, mode: PorterDuff.Mode): Drawable? {
    if (this == null) {
        return null
    }

    val wrappedDrawable = DrawableCompat.wrap(this)
    DrawableCompat.setTintList(wrappedDrawable, color)
    DrawableCompat.setTintMode(wrappedDrawable, mode)
    return wrappedDrawable
}
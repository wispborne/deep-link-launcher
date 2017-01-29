package com.thunderclouddev.deeplink.ui

import android.content.Context
import android.util.TypedValue

/**
 * @author David Whitman on 29 Jan, 2017.
 */
fun Context.attribute(value: Int): TypedValue {
    val ret = TypedValue()
    theme.resolveAttribute(value, ret, true)
    return ret
}

fun Context.attrAsDimen(value: Int): Int = TypedValue.complexToDimensionPixelSize(attribute(value).data, resources.displayMetrics)
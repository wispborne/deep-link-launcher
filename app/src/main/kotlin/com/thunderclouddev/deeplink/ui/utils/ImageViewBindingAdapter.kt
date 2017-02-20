package com.thunderclouddev.deeplink.ui.utils

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.widget.ImageView

/**
 * Sets the given [Bitmap] as the [Drawable] of the input [ImageView]
 *
 * Created by David Whitman on 02 Feb, 2017.
 */
@BindingAdapter("android:src")
fun setImageBitmap(imageView: ImageView, bitmap: Bitmap) {
    imageView.setImageBitmap(bitmap)
}
package com.thunderclouddev.deeplink.ui.utils

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.thunderclouddev.deeplink.ui.qrcode.QrCodeEncoder

/**
 * @author David Whitman on 2/3/2017.
 */
//@BindingAdapter("qrCodeUri")
fun setQrCode(imageView: ImageView, uri: String) {
    imageView.setImageBitmap(QrCodeEncoder().encodeAsBitmap(uri))
}
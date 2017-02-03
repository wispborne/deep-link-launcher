package com.thunderclouddev.deeplink.ui.qrcode

import android.databinding.ObservableField
import android.graphics.Bitmap
import com.google.zxing.WriterException
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import com.thunderclouddev.deeplink.ui.ViewModel
import com.thunderclouddev.deeplink.utils.empty

/**
 * @author David Whitman on 02 Feb, 2017.
 */
class ViewQrCodeViewModel(private val deepLinkInfo: DeepLinkInfo?) : ViewModel {
    val deepLinkString = ObservableField<String>()
    val qrCodeBitmap = ObservableField<Bitmap>()

    override fun onCreate() {
        deepLinkInfo?.let {
            deepLinkString.set(it.deepLink)

            try {
                val qrCodeBitmap = QrCodeEncoder().encodeAsBitmap(it.deepLink)

                if (qrCodeBitmap != null) {
                    this.qrCodeBitmap.set(qrCodeBitmap)
                }
            } catch (writerException: WriterException) {
                TimberKt.w(writerException, { writerException.message ?: String.empty })
            }
        }
    }

    override fun onPause() {}

    override fun onResume() {}

    override fun onDestroy() {}
}
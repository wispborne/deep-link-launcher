package com.thunderclouddev.deeplink.ui.qrcode

import android.databinding.ObservableField
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.ui.ViewModel
import com.thunderclouddev.deeplink.utils.isUri

/**
 * @author David Whitman on 02 Feb, 2017.
 */
class ViewQrCodeViewModel(private val deepLinkInfo: DeepLinkInfo?) : ViewModel() {
    val deepLinkString = ObservableField<String>()

    override fun onCreate() {
        deepLinkInfo?.let {
            if (it.deepLink.isUri()) {
                deepLinkString.set(it.deepLink)
            }
        }
    }
}
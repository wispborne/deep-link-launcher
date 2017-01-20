package com.thunderclouddev.deeplink.barcode

import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.WriterException
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.databinding.QrCodeViewBinding
import com.thunderclouddev.deeplink.empty
import com.thunderclouddev.deeplink.logging.Timber
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.ui.DeepLinkColorizer

/**
 * Created by David Whitman on 11 Jan, 2017.
 */
class ViewQrCodeController(bundle: Bundle) : BaseController(bundle) {
    companion object {
        private val BUNDLE_DEEP_LINK = "BUNDLE_DEEP_LINK"

        fun createController(deepLinkInfo: DeepLinkInfo) =
                ViewQrCodeController(Bundle().apply { putParcelable(BUNDLE_DEEP_LINK, deepLinkInfo) })

    }

    private var stringToEncode: DeepLinkInfo? = null
    private lateinit var colorizer: DeepLinkColorizer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)
        colorizer = DeepLinkColorizer(activity!!)

        val binding = DataBindingUtil.inflate<QrCodeViewBinding>(inflater, R.layout.qr_code_view, container, false)

        stringToEncode = args?.getParcelable<DeepLinkInfo>(BUNDLE_DEEP_LINK)

        stringToEncode?.let {
            binding.encodedString = colorizer.colorize(it.deepLink.toString())
            binding.deepLink = it

            try {
                val qrCodeBitmap = QrCodeEncoder().encodeAsBitmap(it.deepLink.toString())

                if (qrCodeBitmap != null) {
                    binding.qrCodeQrCode.setImageDrawable(BitmapDrawable(resources, qrCodeBitmap))
                }
            } catch (writerException: WriterException) {
                Timber.w(writerException, { writerException.message ?: String.empty })
            }
        }

        return binding.root
    }

    override fun getTitle(): String? {
        return activity?.getString(R.string.qrcode_title)
    }
}
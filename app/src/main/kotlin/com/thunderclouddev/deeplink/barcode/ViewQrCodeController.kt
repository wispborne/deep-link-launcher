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
import com.thunderclouddev.deeplink.ui.BaseController

/**
 * Created by David Whitman on 11 Jan, 2017.
 */
class ViewQrCodeController(bundle: Bundle) : BaseController(bundle) {
    companion object {
        private val BUNDLE_ENCODED_STRING = "BUNDLE_ENCODED_STRING"

        fun createController(stringToEncode: String) =
                ViewQrCodeController(Bundle().apply { putString(BUNDLE_ENCODED_STRING, stringToEncode) })

    }

    private var stringToEncode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)

        val binding = DataBindingUtil.inflate<QrCodeViewBinding>(inflater, R.layout.qr_code_view, container, false)

        stringToEncode = args?.getString(BUNDLE_ENCODED_STRING, null)
        binding.encodedString = stringToEncode

        stringToEncode?.let {
            try {
                val qrCodeBitmap = QrCodeEncoder().encodeAsBitmap(it)

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
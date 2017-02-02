package com.thunderclouddev.deeplink.ui.qrcode

import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.WriterException
import com.thunderclouddev.deeplink.BaseApplication
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.databinding.QrCodeViewBinding
import com.thunderclouddev.deeplink.utils.empty
import com.thunderclouddev.deeplink.ui.JsonSerializer
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.ui.DeepLinkColorizer
import javax.inject.Inject

/**
 * Created by David Whitman on 11 Jan, 2017.
 */
class ViewQrCodeController(bundle: Bundle) : BaseController(bundle) {
    @Inject lateinit var jsonSerializer: JsonSerializer

    companion object {
        private val BUNDLE_DEEP_LINK = "BUNDLE_DEEP_LINK"
    }

    constructor(jsonSerializer: JsonSerializer, deepLinkInfo: DeepLinkInfo)
            : this(Bundle().apply { putString(BUNDLE_DEEP_LINK, jsonSerializer.toJson(deepLinkInfo)) })

    private var stringToEncode: DeepLinkInfo? = null
    private lateinit var colorizer: DeepLinkColorizer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)
        BaseApplication.component.inject(this)
        colorizer = DeepLinkColorizer(activity!!)

        val binding = DataBindingUtil.inflate<QrCodeViewBinding>(inflater, R.layout.qr_code_view, container, false)

        stringToEncode = jsonSerializer.fromJson(args?.getString(BUNDLE_DEEP_LINK) ?: String.empty, DeepLinkInfo::class.java)

        stringToEncode?.let {
            binding.encodedString = colorizer.colorize(it.deepLink.toString())
            binding.deepLink = it

            try {
                val qrCodeBitmap = QrCodeEncoder().encodeAsBitmap(it.deepLink.toString())

                if (qrCodeBitmap != null) {
                    binding.qrCodeQrCode.setImageDrawable(BitmapDrawable(resources, qrCodeBitmap))
                }
            } catch (writerException: WriterException) {
                TimberKt.w(writerException, { writerException.message ?: String.empty })
            }
        }

        return binding.root
    }

    override fun getTitle(): String? {
        return activity?.getString(R.string.qrcode_title)
    }
}
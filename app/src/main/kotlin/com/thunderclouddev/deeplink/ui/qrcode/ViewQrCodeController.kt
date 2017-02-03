package com.thunderclouddev.deeplink.ui.qrcode

import android.app.Activity
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thunderclouddev.deeplink.BaseApp
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.databinding.QrCodeViewBinding
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.utils.empty


/**
 * Display a scannable QR code that represents the given [DeepLinkInfo]
 * Created by David Whitman on 11 Jan, 2017.
 */
class ViewQrCodeController(bundle: Bundle) : BaseController(bundle) {
    private lateinit var viewModel: ViewQrCodeViewModel

    companion object {
        private val BUNDLE_DEEP_LINK = "BUNDLE_DEEP_LINK"
    }

    constructor(deepLinkInfo: DeepLinkInfo)
            : this(Bundle().apply { putString(BUNDLE_DEEP_LINK, BaseApp.component.jsonSerializer.toJson(deepLinkInfo)) })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)

        val deepLinkInfo = BaseApp.component.jsonSerializer.fromJson(args?.getString(ViewQrCodeController.BUNDLE_DEEP_LINK) ?: String.empty, DeepLinkInfo::class.java)
        viewModel = ViewQrCodeViewModel(deepLinkInfo)

        val binding = DataBindingUtil.inflate<QrCodeViewBinding>(inflater, R.layout.qr_code_view, container, false)

        binding.viewModel = viewModel

        viewModel.onCreate()
        return binding.root
    }

    override fun onActivityStopped(activity: Activity) {
        super.onActivityStopped(activity)
        viewModel.onPause()
    }

    override fun onActivityStarted(activity: Activity) {
        super.onActivityStarted(activity)
        viewModel.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

    override fun getTitle(): String? {
        return activity?.getString(R.string.qrcode_title)
    }
}
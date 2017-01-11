package com.thunderclouddev.deeplink.barcode

import android.Manifest
import android.app.Activity
import android.databinding.DataBindingUtil
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.tbruyelle.rxpermissions.RxPermissions
import com.thunderclouddev.deeplink.*
import com.thunderclouddev.deeplink.databinding.ScannerActivityBinding
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.utils.Utilities


/**
 * Created by David Whitman on 07 Jan, 2017.
 */
class ScannerController : BaseController() {
    private val scanCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            Toast.makeText(activity, result?.text, Toast.LENGTH_SHORT).show()

            if (result != null && result.text.isUri()) {
                if (scanContinuously) {
                    val uri = Uri.parse(result.text)
                    if (Utilities.createDeepLinkIntent(uri).hasHandlingActivity(activity!!.packageManager)) {
                        val deepLinkInfo = Utilities.createDeepLinkInfo(uri, activity!!)

                        if (deepLinkInfo != null) {
                            BaseApplication.deepLinkHistory.addLink(deepLinkInfo)
                        }
                    }
                } else {
                    Utilities.resolveAndFire(Uri.parse(result.text), activity!!)
                    router.handleBack()
                }
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
        }

    }

    private var barcodeView: DecoratedBarcodeView? = null

    private var scanContinuously: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)

        val binding = DataBindingUtil.inflate<ScannerActivityBinding>(inflater, R.layout.scanner_activity, container, false)

        barcodeView = binding.scannerBarcodeView
        barcodeView?.setStatusText(String.empty)
        scanContinuously = binding.scannerContinuous.isChecked
        binding.scannerContinuous.setOnCheckedChangeListener { compoundButton, checked ->
            startCapture(checked)
        }

        startCapture(scanContinuously)
        return binding.root
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        barcodeView?.pause()
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        barcodeView?.resume()
    }

    private fun startCapture(scanContinuously: Boolean) {
        val rxPermissions = RxPermissions(activity!!)
        rxPermissions
                .requestEach(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (granted.granted) {
                        if (scanContinuously)
                            barcodeView?.decodeContinuous(scanCallback)
                        else
                            barcodeView?.decodeSingle(scanCallback)
                        barcodeView?.resume()
                    } else {
                        router.handleBack()
                    }
                }
    }

    data class ViewModel(
            val bulkCapture: Boolean
    )
}
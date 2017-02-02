package com.thunderclouddev.deeplink.barcode

import android.Manifest
import android.app.Activity
import android.databinding.BaseObservable
import android.databinding.Bindable
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
import com.thunderclouddev.deeplink.databinding.ScannerViewBinding
import com.thunderclouddev.deeplink.features.DeepLinkLauncher
import com.thunderclouddev.deeplink.interfaces.IDeepLinkHistory
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.utils.Utilities
import javax.inject.Inject


/**
 * Created by David Whitman on 07 Jan, 2017.
 */
class QrScannerController : BaseController() {
    @Inject lateinit var deepLinkHistory: IDeepLinkHistory
    lateinit var deepLinkLauncher: DeepLinkLauncher

    private val scanCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            Toast.makeText(activity, result?.text, Toast.LENGTH_SHORT).show()

            if (result != null && result.text.isUri()) {
                if (model.scanContinuously) {
                    val uri = Uri.parse(result.text)

                    if (Utilities.createDeepLinkIntent(uri).hasAnyHandlingActivity(activity!!.packageManager)) {
                        val deepLinkInfo = Utilities.createDeepLinkRequest(uri, activity!!.packageManager)
                        model.lastScannedUri = deepLinkInfo.deepLink.toString()
                        deepLinkHistory.addLink(deepLinkInfo)
                    }
                } else {
                    deepLinkLauncher.resolveAndFire(Uri.parse(result.text), activity!!)
                    router.handleBack()
                }
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
        }

    }

    private var barcodeView: DecoratedBarcodeView? = null
    private val model = ViewModel(false, String.empty)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)
        BaseApplication.component.inject(this)

        val binding = DataBindingUtil.inflate<ScannerViewBinding>(inflater, R.layout.scanner_view, container, false)

        binding.model = model
        barcodeView = binding.scannerBarcodeView
        barcodeView?.setStatusText(String.empty)
//        binding.scannerContinuous.setOnCheckedChangeListener { compoundButton, checked ->
//            startCapture(checked)
//        }

        startCapture(binding.scannerContinuous.isChecked)
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

    override fun onDestroyView(view: View) {
        barcodeView?.pause()
        super.onDestroyView(view)
    }

    private fun startCapture(scanContinuously: Boolean) {
        val rxPermissions = RxPermissions(activity!!)
        rxPermissions
                .requestEach(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (granted.granted) {
                        barcodeView?.decodeContinuous(scanCallback)
//                        if (scanContinuously)
//                            barcodeView?.decodeContinuous(scanCallback)
//                        else
//                            barcodeView?.decodeSingle(scanCallback)
                        barcodeView?.resume()
                    } else {
                        router.handleBack()
                    }
                }
    }

    class ViewModel(@Bindable var scanContinuously: Boolean,
                    var lastScannedUri: String) : BaseObservable()
}
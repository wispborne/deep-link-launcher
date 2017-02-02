package com.thunderclouddev.deeplink.dagger

import com.thunderclouddev.deeplink.ui.scanner.QrScannerController
import com.thunderclouddev.deeplink.ui.qrcode.ViewQrCodeController
import com.thunderclouddev.deeplink.ui.edit.EditLinkDialog
import com.thunderclouddev.deeplink.ui.home.HomeController
import dagger.Component
import javax.inject.Singleton

/**
 * @author David Whitman on 31 Jan, 2017.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(homeController: HomeController)
    fun inject(homeController: QrScannerController)
    fun inject(editLinkDialog: EditLinkDialog)
    fun inject(creator: EditLinkDialog.Creator)
    fun inject(viewQrCodeController: ViewQrCodeController)
}
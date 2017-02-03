package com.thunderclouddev.deeplink.ui.qrcode

import android.net.Uri
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by David Whitman on 03 Feb, 2017.
 */

class ViewQrCodeViewModelTest {
    lateinit var deepLink: DeepLinkInfo

    @Before
    fun setUp() {
        deepLink = DeepLinkInfo(0, Uri.parse("http://test"), "labelstr", 10000L, listOf())
    }

    @Test
    fun test_onCreate_createsBitmap() {
        val viewModel = ViewQrCodeViewModel(deepLink)
        Assert.assertNull(viewModel.qrCodeBitmap.get())

        viewModel.onCreate()

        Assert.assertNotNull(viewModel.qrCodeBitmap.get())
    }

}
package com.thunderclouddev.deeplink.ui.qrcode

import com.thunderclouddev.deeplink.data.DeepLinkInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by David Whitman on 03 Feb, 2017.
 */
class ViewQrCodeViewModelTest {
    lateinit var deepLinkInfo: DeepLinkInfo

    @Before
    fun setUp() {
        deepLinkInfo = DeepLinkInfo(0, "http://test", "labelstr", 10000L, listOf())
    }

    @Test
    fun test_onCreate_setsNewDeepLinkString() {
        val viewModel = ViewQrCodeViewModel(deepLinkInfo)
        Assert.assertNull(viewModel.deepLinkString.get())

        viewModel.onCreate()

        Assert.assertNotNull(viewModel.deepLinkString.get())
    }
}
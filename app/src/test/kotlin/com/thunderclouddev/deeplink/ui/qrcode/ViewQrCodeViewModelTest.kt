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
    fun `onCreate sets deep link string`() {
        val viewModel = ViewQrCodeViewModel(deepLinkInfo)
        Assert.assertNull(viewModel.deepLinkString.get())

        viewModel.onCreate()

        Assert.assertNotNull(viewModel.deepLinkString.get())
    }

    @Test
    fun `Invalid uri doesn't set deep link string`() {
        val viewModel = ViewQrCodeViewModel(DeepLinkInfo(0, "invalidUri", null, 0, listOf()))

        viewModel.onCreate()

        Assert.assertNull(viewModel.deepLinkString.get())
    }

    @Test
    fun `Null init object doesn't set deep link string`() {
        val viewModel = ViewQrCodeViewModel(null)

        viewModel.onCreate()

        Assert.assertNull(viewModel.deepLinkString.get())
    }
}
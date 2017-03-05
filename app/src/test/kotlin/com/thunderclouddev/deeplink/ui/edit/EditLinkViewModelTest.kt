package com.thunderclouddev.deeplink.ui.edit

import android.databinding.Observable
import android.databinding.ObservableField
import com.thunderclouddev.deeplink.BR
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by David Whitman on 01 Mar, 2017.
 */
@RunWith(MockitoJUnitRunner::class)
class EditLinkViewModelTest {
    @Mock lateinit var handlingAppsForUriFactory: HandlingAppsForUriFactory

    lateinit var viewModel: EditLinkViewModel

    private val createDeepLinkRequest = CreateDeepLinkRequest(
            deepLink = "http://google.com",
            label = "Google",
            updatedTime = 23423423,
            deepLinkHandlers = listOf("com.chrome")
    )

    @Before
    fun setUp() {
        viewModel = EditLinkViewModel(createDeepLinkRequest, handlingAppsForUriFactory)
    }

    @Test fun test_onCreate_updatesPreview() {
        val onPropertyChangedCallback = mock(Observable.OnPropertyChangedCallback::class.java)
        viewModel.addOnPropertyChangedCallback(onPropertyChangedCallback)

        viewModel.onCreate()

        verify(onPropertyChangedCallback).onPropertyChanged(viewModel, BR.fullDeepLink)
        verify(onPropertyChangedCallback, never()).onPropertyChanged(viewModel, BR.authority)
    }

    @Test fun test_onSchemeChanged_previewUpdates() {
        viewModel.scheme.set("newScheme")

        Assert.assertEquals("newScheme://google.com", viewModel.getFullDeepLink())
    }

    @Test fun test_onAuthorityChanged_previewUpdates() {
        viewModel.authority.set("new.auth")

        Assert.assertEquals("http://new.auth", viewModel.getFullDeepLink())
    }

    @Test fun test_onPathChanged_previewUpdates() {
        viewModel.path.set("1/2/3/4")

        Assert.assertEquals("http://google.com/1/2/3/4", viewModel.getFullDeepLink())
    }

    @Test fun test_onQueryParamsChanged_previewUpdates() {
        viewModel.addQueryParam(ObservableField<String>("key1"), ObservableField<String>("value1"))
        viewModel.addQueryParam(ObservableField<String>("key2"), ObservableField<String>("value2"))

        Assert.assertEquals("http://google.com?key1=value1&key2=value2", viewModel.getFullDeepLink())
    }

    @Test fun test_onFragmentChanged_previewUpdates() {
        viewModel.fragment.set("newFragment")

        Assert.assertEquals("http://google.com#newFragment", viewModel.getFullDeepLink())
    }
}
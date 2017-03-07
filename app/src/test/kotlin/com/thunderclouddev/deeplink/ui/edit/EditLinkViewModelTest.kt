package com.thunderclouddev.deeplink.ui.edit

import android.databinding.Observable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.graphics.Bitmap
import com.thunderclouddev.deeplink.BR
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.ui.Uri
import com.thunderclouddev.deeplink.ui.anyKt
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by David Whitman on 01 Mar, 2017.
 */
@RunWith(MockitoJUnitRunner::class)
class EditLinkViewModelTest {
    @Mock lateinit var handlingAppsForUriFactory: HandlingAppsForUriFactory
    @Mock lateinit var mockCallback: ObservableList.OnListChangedCallback<ObservableArrayList<AppViewModel>>

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

    @Test fun `OnCreate updates preview`() {
        val onPropertyChangedCallback = mock(Observable.OnPropertyChangedCallback::class.java)
        viewModel.addOnPropertyChangedCallback(onPropertyChangedCallback)

        viewModel.onCreate()

        verify(onPropertyChangedCallback).onPropertyChanged(viewModel, BR.fullDeepLink)
        verify(onPropertyChangedCallback, never()).onPropertyChanged(viewModel, BR.authority)
    }

    @Test fun `Scheme change updates preview`() {
        viewModel.scheme.set("newScheme")

        Assert.assertEquals("newScheme://google.com", viewModel.getFullDeepLink())
    }

    @Test fun `Authority change updates preview`() {
        viewModel.authority.set("new.auth")

        Assert.assertEquals("http://new.auth", viewModel.getFullDeepLink())
    }

    @Test fun `Path change updates preview`() {
        viewModel.path.set("1/2/3/4")

        Assert.assertEquals("http://google.com/1/2/3/4", viewModel.getFullDeepLink())
    }

    @Test fun `Query param change updates preview`() {
        viewModel.addQueryParam(ObservableField<String>("key1"), ObservableField<String>("value1"))
        viewModel.addQueryParam(ObservableField<String>("key2"), ObservableField<String>("value2"))

        Assert.assertEquals("http://google.com?key1=value1&key2=value2", viewModel.getFullDeepLink())
    }

    @Test fun `Fragment change updates preview`() {
        viewModel.fragment.set("newFragment")

        Assert.assertEquals("http://google.com#newFragment", viewModel.getFullDeepLink())
    }

    @Test fun `Preview change updates handling apps`() {
        val listOfHandlers = listOf(AppViewModel("hello", "google", mock(Bitmap::class.java)))
        `when`(handlingAppsForUriFactory.build(anyKt<Uri>(), ArgumentMatchers.eq(true))).thenReturn(listOfHandlers)
        viewModel.handlingApps.addOnListChangedCallback(mockCallback)

        viewModel.onCreate()
        viewModel.scheme.set("newScheme")

        verify(handlingAppsForUriFactory).build(anyKt(), ArgumentMatchers.anyBoolean())
        verify(mockCallback).onItemRangeInserted(eq(ObservableArrayList<AppViewModel>().apply { addAll(listOfHandlers) }), eq(0), eq(1))
    }
}
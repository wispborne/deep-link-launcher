package com.thunderclouddev.deeplink.ui.edit

import android.databinding.*
import com.thunderclouddev.deeplink.BR
import com.thunderclouddev.deeplink.BaseApp
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.ui.Uri
import com.thunderclouddev.deeplink.ui.ViewModel
import com.thunderclouddev.deeplink.utils.asUri
import com.thunderclouddev.deeplink.utils.empty
import com.thunderclouddev.deeplink.utils.isNotNullOrBlank
import com.thunderclouddev.deeplink.utils.isUri
import javax.inject.Inject

/**
 * @author David Whitman on 2/5/2017.
 */
class EditLinkViewModel(deepLinkInfo: CreateDeepLinkRequest) : BaseObservable(), ViewModel {
    private val uri = deepLinkInfo.deepLink.asUri()!!

    @Bindable var path = ObservableField((uri.path ?: String.empty).removePrefix("/"))
    @Bindable var scheme = ObservableField(uri.scheme)
    @Bindable var authority = ObservableField(uri.authority)
    @Bindable var label = ObservableField(deepLinkInfo.label)
    @Bindable var queryParams = mutableListOf<QueryParamModel>()
    @Bindable var fragment = ObservableField(uri.fragment)
    val handlingApps = ObservableArrayList<AppViewModel>()

    @Inject lateinit var handlingAppsForUriFactory: HandlingAppsForUriFactory

    private val fullDeepLinkNotifierCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            notifyPropertyChanged(BR.fullDeepLink)

            if (getFullDeepLink().isUri()) {
                handlingApps.clear()
                handlingApps.addAll(handlingAppsForUriFactory.build(getFullDeepLink().asUri()!!, defaultOnly = true))
            }
        }
    }

    @Bindable fun getFullDeepLink(): String = Uri.decode(Uri.Builder()
            .scheme(scheme.get())
            .authority(authority.get())
            .path(path.get())
            .query(queryParams
                    .map { it.toString() }
                    .filter(String::isNotBlank)
                    .joinToString(separator = "&"))
            .fragment(fragment.get())
            .toString())

    override fun onCreate() {
        BaseApp.component.inject(this)

        queryParams =
                (if (uri.query.isNotNullOrBlank())
                    uri.queryParameterNames.map {
                        QueryParamModel(
                                ObservableField(it),
                                ObservableField(uri.getQueryParameter(it)),
                                fullDeepLinkNotifierCallback)
                    }
                else emptyList<QueryParamModel>()).toMutableList()

        notifyPropertyChanged(BR.fullDeepLink)

        scheme.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
        authority.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
        path.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
        label.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
        fragment.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
    }

    fun addQueryParam(key: ObservableField<String>, value: ObservableField<String>): QueryParamModel {
        val queryParamModel = QueryParamModel(key, value, fullDeepLinkNotifierCallback)
        queryParams.add(queryParamModel)
        return queryParamModel
    }

    class QueryParamModel(@Bindable var key: ObservableField<String>,
                          @Bindable var value: ObservableField<String>,
                          listener: Observable.OnPropertyChangedCallback) : BaseObservable() {
        init {
            key.addOnPropertyChangedCallback(listener)
            value.addOnPropertyChangedCallback(listener)
        }

        override fun toString(): String {
            return if (key.get().isNullOrBlank()) {
                String.empty
            } else {
                "${Uri.encode(key.get() ?: String.empty)}=${Uri.encode(value.get() ?: String.empty)}"
            }
        }
    }
}
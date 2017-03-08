package com.thunderclouddev.deeplink.ui.edit

import android.databinding.*
import com.thunderclouddev.deeplink.BR
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
class EditLinkViewModel(deepLinkInfo: CreateDeepLinkRequest, handlingAppsForUriFactory: HandlingAppsForUriFactory) : BaseObservable(), ViewModel {
    private val uri = deepLinkInfo.deepLink.asUri()!!

    private val fullDeepLinkNotifierCallback: Observable.OnPropertyChangedCallback by lazy {
        object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                notifyPropertyChanged(BR.fullDeepLink)

                if (getFullDeepLink().isUri()) {
                    val newApps = handlingAppsForUriFactory.build(getFullDeepLink().asUri()!!, defaultOnly = true)

                    // Remove apps no longer in list. Can't use removeAll because it's not part of ObservableArrayList, doesn't notify
                    handlingApps.minus(newApps).forEach { handlingApps.remove(it) }
                    // Then add new apps that aren't already in the list
                    handlingApps.addAll(newApps.minus(handlingApps))
                }
            }
        }
    }

    @Bindable val label = ObservableField(deepLinkInfo.label)
    @Bindable val scheme = ObservableField(uri.scheme)
    @Bindable val authority = ObservableField(uri.authority)
    @Bindable val path = ObservableField((uri.path ?: String.empty).removePrefix("/"))
    @Bindable val queryParams = createDefaultQueryParams()
    @Bindable val fragment = ObservableField(uri.fragment)
    val handlingApps = ObservableArrayList<AppViewModel>()


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

    private fun createDefaultQueryParams(): MutableList<QueryParamModel> {
        return (if (uri.query.isNotNullOrBlank())
            uri.queryParameterNames.map {
                QueryParamModel(
                        ObservableField(it),
                        ObservableField(uri.getQueryParameter(it)),
                        fullDeepLinkNotifierCallback)
            }
        else emptyList<QueryParamModel>()).toMutableList()
    }

    class Factory @Inject constructor(val handlingAppsForUriFactory: HandlingAppsForUriFactory) {
        fun build(deepLinkInfo: CreateDeepLinkRequest) = EditLinkViewModel(deepLinkInfo, handlingAppsForUriFactory)
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
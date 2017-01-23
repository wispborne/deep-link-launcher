package com.thunderclouddev.deeplink.ui.edit

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.databinding.*
import android.databinding.Observable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.thunderclouddev.deeplink.*
import com.thunderclouddev.deeplink.databinding.EditActivityBinding
import com.thunderclouddev.deeplink.databinding.EditQueryStringItemBinding
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import org.jetbrains.anko.enabled
import java.util.*

/**
 * Created by David Whitman on 01 Dec, 2016.
 */
class EditLinkDialog : DialogFragment() {
    private lateinit var binding: EditActivityBinding
    private lateinit var dialogType: DialogType

    private enum class DialogType { ADD, EDIT }

    companion object {
        private val BUNDLE_DEEP_LINK = "BUNDLE_DEEP_LINK"

        fun newInstance(deepLinkToEdit: DeepLinkInfo? = null): EditLinkDialog {
            val args = Bundle().apply { if (deepLinkToEdit != null) putParcelable(BUNDLE_DEEP_LINK, deepLinkToEdit) }
            return EditLinkDialog().apply { arguments = args }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate<EditActivityBinding>(activity!!.layoutInflater, R.layout.edit_activity, null, false)
        val deepLinkInfo = arguments.getParcelable<DeepLinkInfo>(BUNDLE_DEEP_LINK)
                ?: DeepLinkInfo(Uri.EMPTY, String.empty, String.empty, Date().time)
        dialogType = if (arguments.containsKey(BUNDLE_DEEP_LINK)) DialogType.EDIT else DialogType.ADD

        val viewModel = ViewModel(deepLinkInfo)
        binding.info = viewModel

        // Extra blank param pair so user may add new param
        viewModel.addQueryParam(ObservableField<String>(), ObservableField<String>())
        viewModel.queryParams
                .forEach { param ->
                    val (editQueryLayout, view) = createQueryParamRow(viewModel, param)
                    editQueryLayout.addView(view)
                }

        val dialog = buildAlertDialog(viewModel, deepLinkInfo).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).enabled = viewModel.getFullDeepLink().isUri()
        }

        viewModel.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: Observable?, property: Int) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).enabled = viewModel.getFullDeepLink().isUri()
            }

        })
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    private fun createQueryParamRow(viewModel: ViewModel, param: ViewModel.QueryParamModel): Pair<LinearLayout, View> {
        val rowBinding = DataBindingUtil.inflate<EditQueryStringItemBinding>(
                activity!!.layoutInflater, R.layout.edit_query_string_item, null, false)
        rowBinding.param = param
        val editQueryLayout = binding.editQueryLayout
        val onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            if (editQueryLayout.childCount > 0) {
                val lastQueryLayout = editQueryLayout.getChildAt(editQueryLayout.childCount - 1)

                // If the selected field is in the final row, add a query param and renew the querystring UI
                if (lastQueryLayout.findViewById(R.id.edit_key) == view || lastQueryLayout.findViewById(R.id.edit_value) == view) {
                    val newQueryParam = viewModel.addQueryParam(ObservableField<String>(), ObservableField<String>())
                    val (newEditQueryLayout, newView) = createQueryParamRow(viewModel, newQueryParam)
                    editQueryLayout.addView(newView)
                }
            }
        }
        rowBinding.editKey.onFocusChangeListener = onFocusChangeListener
        val view = rowBinding.root
        return Pair(editQueryLayout, view)
    }

    private fun buildAlertDialog(viewModel: ViewModel, deepLinkInfo: DeepLinkInfo): AlertDialog.Builder {
        val builder = AlertDialog.Builder(activity)
                .setView(binding.root)
                .setPositiveButton(R.string.save, { dialog, which ->
                    val deepLink = Uri.parse(viewModel.getFullDeepLink())

                    if (deepLink != null) {
                        BaseApplication.deepLinkHistory.removeLink(deepLinkInfo.id)
                        BaseApplication.deepLinkHistory.addLink(DeepLinkInfo(deepLink, viewModel.label.get(),
                                deepLinkInfo.packageName, updatedTime = Date().time))
                    }
                })
                .setNegativeButton(R.string.cancel, null)

        if (dialogType == DialogType.EDIT) {
            builder.setNeutralButton(R.string.saveAsNew, { dialog, which ->
                val deepLink = Uri.parse(viewModel.getFullDeepLink())
                BaseApplication.deepLinkHistory.addLink(DeepLinkInfo(deepLink, viewModel.label.get(),
                        deepLinkInfo.packageName, updatedTime = Date().time))
            })
        }

        return builder
    }

    class ViewModel(deepLinkInfo: DeepLinkInfo) : BaseObservable() {
        private val uri = deepLinkInfo.deepLink

        private val fullDeepLinkNotifierCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                notifyPropertyChanged(BR.fullDeepLink)
            }
        }

        @Bindable var path = ObservableField((uri.path ?: String.empty).removePrefix("/"))
        @Bindable var scheme = ObservableField(uri.scheme)
        @Bindable var authority = ObservableField(uri.authority)
        @Bindable var label = ObservableField(deepLinkInfo.label)
        @Bindable var queryParams = mutableListOf<QueryParamModel>()
        @Bindable var fragment = ObservableField(uri.fragment)

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

        init {
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
}
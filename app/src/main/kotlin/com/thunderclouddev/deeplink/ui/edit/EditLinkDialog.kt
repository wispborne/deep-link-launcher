package com.thunderclouddev.deeplink.ui.edit

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.databinding.*
import android.net.Uri
import android.os.Bundle
import com.thunderclouddev.deeplink.BR
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.databinding.ActivityEditBinding
import com.thunderclouddev.deeplink.databinding.ItemEditParamRowBinding
import com.thunderclouddev.deeplink.empty
import com.thunderclouddev.deeplink.isNotNullOrBlank
import com.thunderclouddev.deeplink.logging.Timber
import com.thunderclouddev.deeplink.models.DeepLinkInfo

/**
 * Created by David Whitman on 01 Dec, 2016.
 */
class EditLinkDialog : DialogFragment() {
    private lateinit var binding: ActivityEditBinding

    companion object {
        private val BUNDLE_DEEP_LINK = "BUNDLE_DEEP_LINK"

        fun newInstance(deeplink: DeepLinkInfo): EditLinkDialog {
            val frag = EditLinkDialog()
            val args = Bundle()
            args.putString(BUNDLE_DEEP_LINK, DeepLinkInfo.toJson(deeplink))
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate<ActivityEditBinding>(activity!!.layoutInflater, R.layout.activity_edit, null, false)
        val deepLinkInfo = DeepLinkInfo.fromJson(arguments.getString(BUNDLE_DEEP_LINK))

        if (deepLinkInfo == null) {
            Timber.e { "No deep link provided to Edit dialog" }
            dismiss()
        }

        val viewModel = ViewModel(deepLinkInfo!!)
        binding.info = viewModel

        viewModel.queryParams
                .forEach { param ->
                    val rowBinding = DataBindingUtil.inflate<ItemEditParamRowBinding>(
                            activity!!.layoutInflater, R.layout.item_edit_param_row, null, false)
                    rowBinding.param = param
                    binding.editQueryLayout.addView(rowBinding.root)
                }

        val alertDialog = buildAlertDialog()

        return alertDialog.create()
    }

    private fun buildAlertDialog(): AlertDialog.Builder {
        return AlertDialog.Builder(activity)
                .setView(binding.root)
                .setPositiveButton(R.string.save, { dialog, which ->
                    // TODO: Save updated link
                })
                .setNegativeButton(R.string.cancel, null)
    }

    class ViewModel(deepLinkInfo: DeepLinkInfo) : BaseObservable() {
        private val uri = deepLinkInfo.deepLink
        private val scheme = uri.scheme
        private val authority = uri.authority

        @Bindable var path = ObservableField((uri.path ?: String.empty).removePrefix("/"))
        @Bindable var label = ObservableField(deepLinkInfo.activityLabel)
        @Bindable var queryParams = listOf<QueryParamModel>()

        @Bindable fun getFullDeepLink(): String = Uri.decode(Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path.get())
                .query(queryParams.map { it.toString() }.joinToString(separator = "&"))
                .toString())

        init {
            val fullDeepLinkNotifierCallback = object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    notifyPropertyChanged(BR.fullDeepLink)
                }
            }

            queryParams =
                    (if (uri.query.isNotNullOrBlank())
                        uri.queryParameterNames.map {
                            QueryParamModel(
                                    ObservableField(it),
                                    ObservableField(uri.getQueryParameter(it)),
                                    fullDeepLinkNotifierCallback)
                        }
                    else emptyList<QueryParamModel>())

            notifyPropertyChanged(BR.fullDeepLink)

            path.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
            label.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
        }

        class QueryParamModel(@Bindable var key: ObservableField<String>,
                              @Bindable var value: ObservableField<String>,
                              val listener: Observable.OnPropertyChangedCallback) : BaseObservable() {
            init {
                key.addOnPropertyChangedCallback(listener)
                value.addOnPropertyChangedCallback(listener)
            }

            override fun toString(): String = "${Uri.encode(key.get())}=${Uri.encode(value.get())}"
        }
    }
}
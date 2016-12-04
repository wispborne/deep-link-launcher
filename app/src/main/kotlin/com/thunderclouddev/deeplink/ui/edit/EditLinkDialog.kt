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

        binding.info = ViewModel(deepLinkInfo!!)

        deepLinkInfo.deepLink.queryParameterNames
                .associateBy({ it }, { deepLinkInfo.deepLink.getQueryParameter(it) })
                .forEach { param ->
                    val rowBinding = DataBindingUtil.inflate<ItemEditParamRowBinding>(
                            activity!!.layoutInflater, R.layout.item_edit_param_row, null, false)
                    rowBinding.param = param
                    // TODO: Two-way bind the querystring params
                    binding.editQueryLayout.addView(rowBinding.root)
                }

        val alertDialog = AlertDialog.Builder(activity)
                .setTitle(R.string.editDialog_title)
                .setView(binding.root)
                .setPositiveButton(R.string.save, { dialog, which ->
                    // TODO: Save updated link
                })
                .setNegativeButton(R.string.cancel, null)

        return alertDialog.create()
    }

    class ViewModel(deepLinkInfo: DeepLinkInfo) : BaseObservable() {
        private val uri = deepLinkInfo.deepLink
        private val scheme = uri.scheme
        private val authority = uri.authority

        @Bindable var path = ObservableField(uri.path ?: String.empty)
        @Bindable var label = ObservableField(deepLinkInfo.activityLabel)

        var queryParams = emptyMap<String, String>()

        @Bindable fun getFullDeepLink(): String = Uri.decode(Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path.get())
                .query(queryParams.entries.joinToString(separator = "/"))
                .toString())

        init {
            queryParams = if (uri.query.isNotNullOrBlank())
                uri.queryParameterNames.associateBy({ it }, { uri.getQueryParameter(it) })
            else emptyMap<String, String>()

            notifyPropertyChanged(BR.fullDeepLink)

            val fullDeepLinkNotifierCallback = object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    notifyPropertyChanged(BR.fullDeepLink)
                }
            }

            path.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
            label.addOnPropertyChangedCallback(fullDeepLinkNotifierCallback)
        }
    }
}
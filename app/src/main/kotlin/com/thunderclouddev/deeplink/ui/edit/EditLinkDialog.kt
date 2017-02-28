package com.thunderclouddev.deeplink.ui.edit

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.thunderclouddev.deeplink.BaseApp
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.data.DeepLinkHistory
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.databinding.EditQueryStringItemBinding
import com.thunderclouddev.deeplink.databinding.EditViewBinding
import com.thunderclouddev.deeplink.ui.JsonSerializer
import com.thunderclouddev.deeplink.ui.Uri
import com.thunderclouddev.deeplink.utils.Utilities
import com.thunderclouddev.deeplink.utils.empty
import com.thunderclouddev.deeplink.utils.handlingActivities
import com.thunderclouddev.deeplink.utils.isUri
import org.jetbrains.anko.enabled
import java.util.*
import javax.inject.Inject

/**
 * Displays a dialog that lets the user either edit an existing deep link or create a new one.
 * Input is validated before being updated in the database.
 *
 * TODO: This is not very MVVM - need to move more logic to the view model
 *
 * @author David Whitman on 29 Jan, 2017.
 */
class EditLinkDialog : DialogFragment() {
    @Inject lateinit var jsonSerializer: JsonSerializer
    @Inject lateinit var deepLinkHistory: DeepLinkHistory

    private lateinit var binding: EditViewBinding
    private lateinit var dialogType: DialogType

    private enum class DialogType { ADD, EDIT }

    companion object {
        private val BUNDLE_DEEP_LINK = "BUNDLE_DEEP_LINK"
    }

    class Creator {
        @Inject lateinit var jsonSerializer: JsonSerializer

        fun newInstance(deepLinkToEdit: DeepLinkInfo? = null): EditLinkDialog {
            val args = Bundle().apply { if (deepLinkToEdit != null) putString(BUNDLE_DEEP_LINK, BaseApp.component.json.toJson(deepLinkToEdit)) }
            return EditLinkDialog().apply { arguments = args }
        }
    }

    init {
        BaseApp.component.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate<EditViewBinding>(activity!!.layoutInflater, R.layout.edit_view, null, false)
        val deepLinkInfo: DeepLinkInfo? = jsonSerializer.fromJson(arguments.getString(BUNDLE_DEEP_LINK), DeepLinkInfo::class.java)
        val createDeepLinkRequest = deepLinkInfo?.let(::CreateDeepLinkRequest)
                ?: CreateDeepLinkRequest(String.empty, String.empty, Date().time, emptyList())
        dialogType = if (arguments.containsKey(BUNDLE_DEEP_LINK)) DialogType.EDIT else DialogType.ADD

        val viewModel = EditLinkViewModel(createDeepLinkRequest)
        viewModel.onCreate()
        binding.viewModel = viewModel
        binding.editDialogHandlingAppsRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.editDialogHandlingAppsRecyclerView.adapter = HandlingAppsAdapter(activity, viewModel.handlingApps)

        // Extra blank param pair so user may add new param
        viewModel.addQueryParam(ObservableField<String>(), ObservableField<String>())
        viewModel.queryParams
                .forEach { param ->
                    val view = createQueryParamRow(viewModel, param, binding.editQueryLayout)
                    binding.editQueryLayout.addView(view)
                }

        val dialog = buildAlertDialog(viewModel, deepLinkInfo?.id).create()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).enabled = viewModel.getFullDeepLink().isUri()
        }

        viewModel.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: Observable?, property: Int) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).enabled = viewModel.getFullDeepLink().isUri()
            }
        })
        return dialog
    }

    private fun createQueryParamRow(viewModel: EditLinkViewModel, param: EditLinkViewModel.QueryParamModel, layoutToAddTo: ViewGroup): View {
        val rowBinding = DataBindingUtil.inflate<EditQueryStringItemBinding>(
                activity!!.layoutInflater, R.layout.edit_query_string_item, null, false)
        rowBinding.param = param
        val onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            if (layoutToAddTo.childCount > 0) {
                val lastQueryLayout = layoutToAddTo.getChildAt(layoutToAddTo.childCount - 1)

                // If the selected field is in the final row, add a query param and renew the querystring UI
                if (lastQueryLayout.findViewById(R.id.edit_key) == view || lastQueryLayout.findViewById(R.id.edit_value) == view) {
                    val newQueryParam = viewModel.addQueryParam(ObservableField<String>(), ObservableField<String>())
                    val newView = createQueryParamRow(viewModel, newQueryParam, layoutToAddTo)
                    layoutToAddTo.addView(newView)
                }
            }
        }
        rowBinding.editKey.onFocusChangeListener = onFocusChangeListener

        return rowBinding.root
    }

    private fun buildAlertDialog(viewModel: EditLinkViewModel, existingId: Long? = null): AlertDialog.Builder {
        val builder = AlertDialog.Builder(activity)
                .setView(binding.root)
                .setPositiveButton(R.string.save, { dialog, which ->
                    val deepLinkUri = Uri.parse(viewModel.getFullDeepLink())

                    if (deepLinkUri != null) {
                        if (existingId != null) {
                            deepLinkHistory.removeLink(existingId)
                        }

                        deepLinkHistory.addLink(CreateDeepLinkRequest(
                                deepLink = deepLinkUri.toString(), label = viewModel.label.get(),
                                updatedTime = Date().time,
                                deepLinkHandlers = findHandlersForUri(deepLinkUri)))
                    }
                })
                .setNegativeButton(R.string.cancel, null)

        if (dialogType == DialogType.EDIT) {
            builder.setNeutralButton(R.string.saveAsNew, { dialog, which ->
                val deepLinkUri = Uri.parse(viewModel.getFullDeepLink())

                if (deepLinkUri != null) {
                    deepLinkHistory.addLink(CreateDeepLinkRequest(
                            deepLink = deepLinkUri.toString(), label = viewModel.label.get(),
                            updatedTime = Date().time, deepLinkHandlers = findHandlersForUri(deepLinkUri)))
                }
            })
        }

        return builder
    }

    private fun findHandlersForUri(deepLinkUri: Uri) = Utilities.createDeepLinkIntent(deepLinkUri)
            .handlingActivities(activity.packageManager)
            .map { it.activityInfo.packageName ?: String.empty }
}
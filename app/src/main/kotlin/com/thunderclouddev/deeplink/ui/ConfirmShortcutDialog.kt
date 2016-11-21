package com.thunderclouddev.deeplink.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.utils.Utilities
import kotlinx.android.synthetic.main.confirm_shortcut_dialog.*

class ConfirmShortcutDialog : DialogFragment() {

    private val deepLink by lazy { arguments.getString(KEY_DEEP_LINK) }
    private val defaultLabel by lazy { arguments.getString(KEY_LABEL, "") }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.confirm_shortcut_dialog, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (defaultLabel.isNotEmpty()) {
            shortcut_label.setText(defaultLabel)
            shortcut_label.setSelection(defaultLabel!!.length)
        }

        confirm_shortcut_negative.setOnClickListener { dismiss() }
        confirm_shortcut_positive.setOnClickListener {
            val shortcutAdded = Utilities.addShortcut(deepLink, activity, shortcut_label.text.toString())
            if (shortcutAdded) {
                Toast.makeText(activity, "shortcut added", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "could not add shortcut", Toast.LENGTH_LONG).show()
            }
            dismiss()
        }
    }

    companion object {
        private val KEY_DEEP_LINK = "key_deep_link"
        private val KEY_LABEL = "key_label"

        fun newInstance(deepLinkUri: String, defaultLabel: String): ConfirmShortcutDialog {
            val dialog = ConfirmShortcutDialog()

            val args = Bundle()
            args.putString(KEY_DEEP_LINK, deepLinkUri)
            args.putString(KEY_LABEL, defaultLabel)
            dialog.arguments = args

            return dialog
        }
    }
}

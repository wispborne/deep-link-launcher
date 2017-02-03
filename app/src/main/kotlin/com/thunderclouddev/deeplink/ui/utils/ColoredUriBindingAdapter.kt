package com.thunderclouddev.deeplink.ui.utils

import android.databinding.BindingAdapter
import android.widget.TextView
import com.thunderclouddev.deeplink.ui.DeepLinkColorizer

/**
 * Adapter to automatically color Uri in the view.
 * TODO: Avoid creating a new instance of [DeepLinkColorizer] every time
 *
 * Created by David Whitman on 03 Feb, 2017.
 */
@BindingAdapter("binding:coloredUri")
fun setText(textView: TextView, string: String) {
    textView.text = DeepLinkColorizer(textView.context).colorize(string)
}
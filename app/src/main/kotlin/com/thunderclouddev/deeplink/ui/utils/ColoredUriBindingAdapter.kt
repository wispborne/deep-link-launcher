package com.thunderclouddev.deeplink.ui.utils

import android.databinding.BindingAdapter
import android.widget.TextView
import com.thunderclouddev.deeplink.ui.DeepLinkColorizer

/**
 * Adapter to automatically color Uri in the view.
 * TODO: Avoid creating a new instance of [DeepLinkColorizer] every time
 *
 * TODO: Switch to [BindingAdapter] when [https://youtrack.jetbrains.com/issue/KT-16179] is fixed
 *
 * Created by David Whitman on 03 Feb, 2017.
 */
//@BindingAdapter("coloredUri")
//fun setText(textView: TextView, string: String) {
//    textView.text = DeepLinkColorizer(textView.context).colorize(string)
//}
fun setColoredText(textView: TextView, string: String) {
    textView.text = DeepLinkColorizer(textView.context).colorize(string)
}
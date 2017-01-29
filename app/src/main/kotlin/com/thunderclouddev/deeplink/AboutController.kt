package com.thunderclouddev.deeplink

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thunderclouddev.deeplink.databinding.AboutViewBinding
import com.thunderclouddev.deeplink.ui.BaseController

/**
 * @author David Whitman on 29 Jan, 2017.
 */
class AboutController(bundle: Bundle? = null) : BaseController(bundle) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)
        val binding = DataBindingUtil.inflate<AboutViewBinding>(inflater, R.layout.about_view, container, false)
        binding.model = ViewModel(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString())
        return binding.root
    }

    override fun getTitle(): String? {
        return activity?.getString(R.string.menu_about_title)
    }

    internal data class ViewModel(val versionName: String,
                                  val versionCode: String)
}
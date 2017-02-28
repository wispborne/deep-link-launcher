package com.thunderclouddev.deeplink.ui.edit

import android.graphics.Bitmap
import com.thunderclouddev.deeplink.ui.BaseRecyclerViewAdapter

/**
 * @author David Whitman on 27 Feb, 2017.
 */
data class AppViewModel(val packageName: String, val appName: String, val icon: Bitmap) : BaseRecyclerViewAdapter.ViewModel
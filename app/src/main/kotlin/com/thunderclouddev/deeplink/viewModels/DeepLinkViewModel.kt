package com.thunderclouddev.deeplink.viewModels

import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.ui.SortedListAdapter

/**
 * Created by David Whitman on 24 Nov, 2016.
 */
data class DeepLinkViewModel(val deepLinkInfo: DeepLinkInfo) : SortedListAdapter.ViewModel
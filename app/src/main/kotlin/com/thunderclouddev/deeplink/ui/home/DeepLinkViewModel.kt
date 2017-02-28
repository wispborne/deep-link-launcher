package com.thunderclouddev.deeplink.ui.home

import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.ui.BaseRecyclerViewAdapter
import com.thunderclouddev.deeplink.utils.empty
import com.thunderclouddev.deeplink.utils.firstOr
import java.util.*

/**
 * Created by David Whitman on 24 Nov, 2016.
 */
data class DeepLinkViewModel(val deepLinkInfo: DeepLinkInfo) : BaseRecyclerViewAdapter.ViewModel {
    var showingContextMenu = false

    class DefaultComparator : Comparator<DeepLinkViewModel> {
        override fun compare(left: DeepLinkViewModel, right: DeepLinkViewModel): Int {
            val packageComparison = left.deepLinkInfo.deepLinkHandlers.firstOr(String.empty)
                    .compareTo(right.deepLinkInfo.deepLinkHandlers.firstOr(String.empty), true)

            return if (packageComparison != 0)
                packageComparison
            else
                left.deepLinkInfo.deepLink.compareTo(right.deepLinkInfo.deepLink)
        }
    }
}
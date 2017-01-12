package com.thunderclouddev.deeplink.viewModels

import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.ui.SortedListAdapter
import java.util.*

/**
 * Created by David Whitman on 24 Nov, 2016.
 */
data class DeepLinkViewModel(val deepLinkInfo: DeepLinkInfo) : SortedListAdapter.ViewModel {
    class DefaultComparator : Comparator<DeepLinkViewModel> {
        override fun compare(left: DeepLinkViewModel, right: DeepLinkViewModel): Int {
            val packageComparison = left.deepLinkInfo.packageName.compareTo(right.deepLinkInfo.packageName, true)

            return if (packageComparison != 0)
                packageComparison
            else
                left.deepLinkInfo.deepLink.compareTo(right.deepLinkInfo.deepLink)
        }
    }
}
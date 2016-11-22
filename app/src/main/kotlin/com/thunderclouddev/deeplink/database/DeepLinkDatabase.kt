package com.thunderclouddev.deeplink.database

import com.thunderclouddev.deeplink.models.DeepLinkInfo

/**
 * @author David Whitman on Nov 22, 2016.
 */
interface DeepLinkDatabase {
    fun addLinkToHistory(deepLinkInfo: DeepLinkInfo): String
    fun removeLinkFromHistory(deepLinkId: String)
    fun clearAllHistory()
    fun addListener(listener: Listener): Int
    fun removeListener(id: Int)

    interface Listener {
        fun onDataChanged(dataSnapshot: List<DeepLinkInfo>)
    }
}
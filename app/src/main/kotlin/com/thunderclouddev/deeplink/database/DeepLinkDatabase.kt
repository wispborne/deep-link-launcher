package com.thunderclouddev.deeplink.database

import com.thunderclouddev.deeplink.models.DeepLinkInfo

/**
 * @author David Whitman on Nov 22, 2016.
 */
interface DeepLinkDatabase {
    fun putLink(deepLinkInfo: DeepLinkInfo): String
    fun removeLink(deepLinkId: String)
    fun clearAllHistory()
    fun addListener(listener: Listener): Int
    fun removeListener(id: Int)

    interface Listener {
        fun onDataChanged(dataSnapshot: List<DeepLinkInfo>)
    }
}
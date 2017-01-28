package com.thunderclouddev.deeplink.database

import android.net.Uri
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.models.DeepLinkInfo

/**
 * @author David Whitman on Nov 22, 2016.
 */
interface DeepLinkDatabase {
    fun putLink(request: CreateDeepLinkRequest): Long
    fun getLink(deepLinkId: Long): DeepLinkInfo?
    fun removeLink(deepLinkId: Long)
    fun clearAllHistory()
    fun addListener(listener: Listener): Int
    fun removeListener(id: Int)
    fun containsLink(deepLink: Uri): Boolean

    interface Listener {
        fun onDataChanged(dataSnapshot: List<DeepLinkInfo>)

    }
}
package com.thunderclouddev.deeplink.interfaces

import android.net.Uri
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest

interface IDeepLinkHistory {
    fun addLink(deepLinkInfo: CreateDeepLinkRequest)
    fun removeLink(deepLinkId: Long)
    fun clearAll()
    fun addListener(listener: DeepLinkDatabase.Listener): Int
    fun removeListener(id: Int)
    fun containsLink(deepLink: Uri): Boolean
}

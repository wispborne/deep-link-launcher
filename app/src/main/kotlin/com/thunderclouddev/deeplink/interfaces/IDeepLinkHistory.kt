package com.thunderclouddev.deeplink.interfaces

import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.models.DeepLinkInfo

interface IDeepLinkHistory {
    fun addLink(deepLinkInfo: DeepLinkInfo)

    fun removeLink(deepLinkId: String)

    fun clearAll()

    fun addListener(listener: DeepLinkDatabase.Listener): Int
    fun removeListener(id: Int)
}

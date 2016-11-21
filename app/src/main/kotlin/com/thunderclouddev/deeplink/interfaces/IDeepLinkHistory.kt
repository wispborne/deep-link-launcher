package com.thunderclouddev.deeplink.interfaces

import com.thunderclouddev.deeplink.models.DeepLinkInfo

interface IDeepLinkHistory {
    fun addLinkToHistory(deepLinkInfo: DeepLinkInfo)

    fun removeLinkFromHistory(deepLinkId: String)

    fun clearAllHistory()

    val linkHistoryFromFileSystem: List<DeepLinkInfo>
}

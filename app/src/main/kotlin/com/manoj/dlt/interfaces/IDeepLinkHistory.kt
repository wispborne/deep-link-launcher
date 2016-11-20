package com.manoj.dlt.interfaces

import com.manoj.dlt.models.DeepLinkInfo

interface IDeepLinkHistory {
    fun addLinkToHistory(deepLinkInfo: DeepLinkInfo)

    fun removeLinkFromHistory(deepLinkId: String)

    fun clearAllHistory()

    val linkHistoryFromFileSystem: List<DeepLinkInfo>
}

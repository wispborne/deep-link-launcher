package com.thunderclouddev.deeplink.data

/**
 * Created by David Whitman on 23 Jan, 2017.
 */
data class CreateDeepLinkRequest(
        val deepLink: String,
        val label: String?,
        val updatedTime: Long,
        val deepLinkHandlers: List<String>) {
    constructor(deepLinkInfo: DeepLinkInfo)
            : this(deepLinkInfo.deepLink, deepLinkInfo.label, deepLinkInfo.updatedTime, deepLinkInfo.deepLinkHandlers)
}
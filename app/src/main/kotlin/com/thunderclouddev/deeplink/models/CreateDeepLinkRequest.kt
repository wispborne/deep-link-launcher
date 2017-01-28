package com.thunderclouddev.deeplink.models

import android.net.Uri

/**
 * Created by David Whitman on 23 Jan, 2017.
 */
data class CreateDeepLinkRequest(
        val deepLink: Uri,
        val label: String?,
        val updatedTime: Long,
        val deepLinkHandlers: List<String>) {
    constructor(deepLinkInfo: DeepLinkInfo)
            : this(deepLinkInfo.deepLink, deepLinkInfo.label, deepLinkInfo.updatedTime, deepLinkInfo.deepLinkHandlers)
}
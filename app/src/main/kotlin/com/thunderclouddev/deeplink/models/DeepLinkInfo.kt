package com.thunderclouddev.deeplink.models

import android.net.Uri

data class DeepLinkInfo(val id: Long,
                        val deepLink: Uri,
                        val label: String?,
                        val updatedTime: Long,
                        val deepLinkHandlers: List<String>) : Comparable<DeepLinkInfo> {

    override fun compareTo(other: DeepLinkInfo) = if (this.updatedTime < other.updatedTime) 1 else -1
}

package com.thunderclouddev.deeplink.data

data class DeepLinkInfo(val id: Long,
                        val deepLink: String,
                        val label: String?,
                        val updatedTime: Long,
                        val deepLinkHandlers: List<String>) : Comparable<DeepLinkInfo> {

    override fun compareTo(other: DeepLinkInfo) = if (this.updatedTime < other.updatedTime) 1 else -1
}

package com.thunderclouddev.deeplink.data

import android.net.Uri
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.data.DeepLinkDatabase

class DeepLinkHistory(private val database: DeepLinkDatabase) {
    fun addLink(deepLinkInfo: CreateDeepLinkRequest) {
        database.putLink(deepLinkInfo)
    }

    fun removeLink(deepLinkId: Long) {
        database.removeLink(deepLinkId)
    }

    fun containsLink(deepLink: String): Boolean {
        return database.containsLink(deepLink)
    }

    fun clearAll() {
        database.clearAllHistory()
    }

    fun addListener(listener: DeepLinkDatabase.Listener): Int {
        return database.addListener(listener)
    }

    fun removeListener(id: Int) {
        database.removeListener(id)
    }
}

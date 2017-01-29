package com.thunderclouddev.deeplink.features

import android.net.Uri
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.interfaces.IDeepLinkHistory
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest

class DeepLinkHistory(private val database: DeepLinkDatabase) : IDeepLinkHistory {
    override fun addLink(deepLinkInfo: CreateDeepLinkRequest) {
        database.putLink(deepLinkInfo)
    }

    override fun removeLink(deepLinkId: Long) {
        database.removeLink(deepLinkId)
    }

    override fun containsLink(deepLink: Uri): Boolean {
        return database.containsLink(deepLink)
    }

    override fun clearAll() {
        database.clearAllHistory()
    }

    override fun addListener(listener: DeepLinkDatabase.Listener): Int {
        return database.addListener(listener)
    }

    override fun removeListener(id: Int) {
        database.removeListener(id)
    }
}

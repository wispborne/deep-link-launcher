package com.thunderclouddev.deeplink.features

import android.net.Uri
import com.thunderclouddev.deeplink.BaseApplication
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.events.DeepLinkLaunchedEvent
import com.thunderclouddev.deeplink.interfaces.IDeepLinkHistory
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest
import org.greenrobot.eventbus.Subscribe

class DeepLinkHistory(private val database: DeepLinkDatabase) : IDeepLinkHistory {
    init {
        BaseApplication.bus.register(this)
    }

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

    @Subscribe(sticky = true, priority = 1)
    fun onEvent(deepLinkFireEvent: DeepLinkLaunchedEvent) {
        addLink(deepLinkFireEvent.createDeepLinkRequest)
    }
}

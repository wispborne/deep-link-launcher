package com.thunderclouddev.deeplink.features

import com.thunderclouddev.deeplink.BaseApplication
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.events.DeepLinkFireEvent
import com.thunderclouddev.deeplink.interfaces.IDeepLinkHistory
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DeepLinkHistory(val database: DeepLinkDatabase) : IDeepLinkHistory {
    init {
        BaseApplication.bus.register(this)
    }

    override fun addLink(deepLinkInfo: DeepLinkInfo) {
        database.putLink(deepLinkInfo)
    }

    override fun removeLink(deepLinkId: String) {
        database.removeLink(deepLinkId)
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
    fun onEvent(deepLinkFireEvent: DeepLinkFireEvent) {
        if (deepLinkFireEvent.resultType == ResultType.SUCCESS) {
            addLink(deepLinkFireEvent.info)
        }
    }
}

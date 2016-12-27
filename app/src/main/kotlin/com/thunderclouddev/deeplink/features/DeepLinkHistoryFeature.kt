package com.thunderclouddev.deeplink.features

import android.content.Context
import com.thunderclouddev.deeplink.BaseApplication
import com.thunderclouddev.deeplink.Constants
import com.thunderclouddev.deeplink.DbConstants
import com.thunderclouddev.deeplink.events.DeepLinkFireEvent
import com.thunderclouddev.deeplink.interfaces.IDeepLinkHistory
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class DeepLinkHistoryFeature private constructor(private val _context: Context) : IDeepLinkHistory {
    private val _fileSystem: FileSystem

    init {
        _fileSystem = FileSystem(_context, Constants.DEEP_LINK_HISTORY_KEY)
        EventBus.getDefault().register(this)
    }

    override fun addLinkToHistory(deepLinkInfo: DeepLinkInfo) {
        BaseApplication.database.putLink(deepLinkInfo)
    }

    override fun removeLinkFromHistory(deepLinkId: String) {
        BaseApplication.database.removeLink(deepLinkId)
    }

    override fun clearAllHistory() {
        BaseApplication.database.clearAllHistory()
    }

    override val linkHistoryFromFileSystem: List<DeepLinkInfo>
        get() {
            return _fileSystem.values()
                    .mapNotNull { DeepLinkInfo.fromJson(it) }
                    .sorted()
        }

    @Subscribe(sticky = true, priority = 1)
    fun onEvent(deepLinkFireEvent: DeepLinkFireEvent) {
        if (deepLinkFireEvent.resultType == ResultType.SUCCESS) {
            addLinkToHistory(deepLinkFireEvent.info)
        }
    }

    private fun clearFileSystemHistory() {
        _fileSystem.clearAll()
    }

    companion object {
        private var _instance: DeepLinkHistoryFeature? = null

        fun getInstance(context: Context): DeepLinkHistoryFeature {
            if (_instance == null) {
                _instance = DeepLinkHistoryFeature(context)
            }

            return _instance as DeepLinkHistoryFeature
        }
    }
}

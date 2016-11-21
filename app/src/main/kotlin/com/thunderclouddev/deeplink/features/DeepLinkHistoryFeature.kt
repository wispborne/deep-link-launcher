package com.thunderclouddev.deeplink.features

import android.content.Context
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
        migrateHistoryToFirebase()
        EventBus.getDefault().register(this)
    }

    override fun addLinkToHistory(deepLinkInfo: DeepLinkInfo) {
        if (Constants.isFirebaseAvailable(_context)) {
            addLinkToFirebaseHistory(deepLinkInfo)
        } else {
            addLinkToFileSystemHistory(deepLinkInfo)
        }
    }

    override fun removeLinkFromHistory(deepLinkId: String) {
        if (Constants.isFirebaseAvailable(_context)) {
            removeLinkFromFirebaseHistory(deepLinkId)
        } else {
            removeLinkFromFileSystemHistory(deepLinkId)
        }
    }

    override fun clearAllHistory() {
        if (Constants.isFirebaseAvailable(_context)) {
            clearFirebaseHistory()
        } else {
            clearFileSystemHistory()
        }
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

    private fun addLinkToFileSystemHistory(deepLinkInfo: DeepLinkInfo) {
        _fileSystem.write(deepLinkInfo.id, DeepLinkInfo.toJson(deepLinkInfo))
    }

    private fun addLinkToFirebaseHistory(deepLinkInfo: DeepLinkInfo) {
        val baseUserReference = ProfileFeature.getInstance(_context).currentUserFirebaseBaseRef
        val linkReference = baseUserReference.child(DbConstants.USER_HISTORY).child(deepLinkInfo.id)
        val infoMap = object : HashMap<String, Any>() {
            init {
                put(DbConstants.DL_ACTIVITY_LABEL, deepLinkInfo.activityLabel)
                put(DbConstants.DL_DEEP_LINK, deepLinkInfo.deepLink)
                put(DbConstants.DL_PACKAGE_NAME, deepLinkInfo.packageName)
                put(DbConstants.DL_UPDATED_TIME, deepLinkInfo.updatedTime)
            }
        }
        linkReference.setValue(infoMap)
    }

    private fun clearFileSystemHistory() {
        _fileSystem.clearAll()
    }

    private fun clearFirebaseHistory() {
        val baseUserReference = ProfileFeature.getInstance(_context).currentUserFirebaseBaseRef
        val historyRef = baseUserReference.child(DbConstants.USER_HISTORY)
        historyRef.setValue(null)
    }

    private fun removeLinkFromFileSystemHistory(deepLinkId: String) {
        _fileSystem.clear(deepLinkId)
    }

    private fun removeLinkFromFirebaseHistory(deepLinkId: String) {
        val baseUserReference = ProfileFeature.getInstance(_context).currentUserFirebaseBaseRef
        val linkReference = baseUserReference.child(DbConstants.USER_HISTORY).child(deepLinkId)
        linkReference.setValue(null)
    }

    private fun migrateHistoryToFirebase() {
        if (Constants.isFirebaseAvailable(_context)) {
            for (info in linkHistoryFromFileSystem) {
                addLinkToHistory(info)
            }
            clearFileSystemHistory()
        }
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

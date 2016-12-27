package com.thunderclouddev.deeplink.database

import android.content.Context
import com.thunderclouddev.deeplink.Constants
import com.thunderclouddev.deeplink.features.FileSystem
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import java.util.*

class SharedPrefsDeepLinkDatabase(context: Context) : DeepLinkDatabase {
    private val fileSystem = FileSystem(context, Constants.DEEP_LINK_HISTORY_KEY)
    private val listeners = mutableMapOf<Int, DeepLinkDatabase.Listener>()

    override fun addListener(listener: DeepLinkDatabase.Listener): Int {
        val id = Random().nextInt()
        listeners.put(id, listener)
        notifyListeners()
        return id
    }

    override fun removeListener(id: Int) {
        listeners.remove(id)
    }

    override fun putLink(deepLinkInfo: DeepLinkInfo): String {
        fileSystem.write(deepLinkInfo.id, DeepLinkInfo.toJson(deepLinkInfo))

        notifyListeners()
        return deepLinkInfo.id
    }

    override fun removeLink(deepLinkId: String) {
        fileSystem.clear(deepLinkId)
        notifyListeners()
    }

    override fun clearAllHistory() {
        fileSystem.clearAll()
    }

    private fun notifyListeners() {
        val data = fileSystem.all().values
                .map { DeepLinkInfo.fromJson(it) }
                .filterNotNull()
        listeners.values.forEach { it.onDataChanged(data) }
    }
}
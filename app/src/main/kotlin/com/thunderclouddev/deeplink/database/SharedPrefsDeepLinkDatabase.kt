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
        notifyListener(id)
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
        listeners.values.forEach { it.onDataChanged(fetchData()) }
    }

    private fun notifyListener(id: Int) = listeners[id]?.onDataChanged(fetchData())

    private fun fetchData(): List<DeepLinkInfo> = fileSystem.all().values
            .map { DeepLinkInfo.fromJson(it) }
            .filterNotNull()
}
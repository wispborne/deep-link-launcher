package com.thunderclouddev.deeplink.database

import android.content.Context
import android.net.Uri
import com.thunderclouddev.deeplink.Constants
import com.thunderclouddev.deeplink.features.FileSystem
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import java.util.*

class SharedPrefsDeepLinkDatabase(context: Context) : DeepLinkDatabase {
    private val fileSystem = FileSystem(context, Constants.DEEP_LINK_HISTORY_KEY)
    private val listeners = mutableMapOf<Int, DeepLinkDatabase.Listener>()

    private val currentSchemaVersion = 3
    private val deepLinkInfoSerializer = DeepLinkInfoJsonSerializer()
    override fun addListener(listener: DeepLinkDatabase.Listener): Int {
        val id = Random().nextInt()
        listeners.put(id, listener)
        notifyListener(id)
        return id
    }

    override fun removeListener(id: Int) {
        listeners.remove(id)
    }

    override fun putLink(request: CreateDeepLinkRequest): Long {
        val newId = generateId(request)
        val newDeepLinkInfo = DeepLinkInfo(newId, request.deepLink, request.label, request.updatedTime, request.deepLinkHandlers)
        fileSystem.write(newId.toString(), deepLinkInfoSerializer.toJson(newDeepLinkInfo, currentSchemaVersion).toString())

        notifyListeners()
        return newId
    }

    override fun getLink(deepLinkId: Long) = fileSystem.all()[deepLinkId.toString()]?.let { deepLinkInfoSerializer.fromJson(it) }

    override fun removeLink(deepLinkId: Long) {
        fileSystem.clear(deepLinkId.toString())
        notifyListeners()
    }

    override fun clearAllHistory() {
        fileSystem.clearAll()
    }

    override fun containsLink(deepLink: Uri) = fileSystem.all()
            .filterValues {
                deepLink == deepLinkInfoSerializer.fromJson(it)?.deepLink
            }
            .any()

    private fun generateId(createDeepLinkRequest: CreateDeepLinkRequest): Long {
        return Random().nextLong()
        // unique id for each deep link entry. similar deep links, varying in query or fragments are combined
//        var id = createDeepLinkRequest.deepLink.toString()
//        if (createDeepLinkRequest.deepLink.fragment != null) {
//            id = id.replace(createDeepLinkRequest.deepLink.fragment, "").replace("#", "")
//        }
//        if (createDeepLinkRequest.deepLink.query != null) {
//            id = id.replace(createDeepLinkRequest.deepLink.query, "").replace("?", "")
//        }
//        id = id.replace("/", "")
//        //replace '.' since firebase does not support them in paths
//        id = id.replace(".", "-dot-")
//        return id
    }

    private fun notifyListeners() {
        listeners.values.forEach { it.onDataChanged(fetchData()) }
    }

    private fun notifyListener(id: Int) = listeners[id]?.onDataChanged(fetchData())

    private fun fetchData(): List<DeepLinkInfo> = fileSystem.all().values
            .map { deepLinkInfoSerializer.fromJson(it) }
            .filterNotNull()
}
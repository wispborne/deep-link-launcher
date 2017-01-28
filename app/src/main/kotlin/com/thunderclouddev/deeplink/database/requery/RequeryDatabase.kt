package com.thunderclouddev.deeplink.database.requery

import android.content.Context
import android.net.Uri
import com.thunderclouddev.deeplink.BuildConfig
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.kotlin.invoke
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode
import java.util.*

/**
 * Created by David Whitman on 28 Jan, 2017.
 */
class RequeryDatabase(context: Context) : DeepLinkDatabase {
    private val listeners = mutableMapOf<Int, DeepLinkDatabase.Listener>()

    private val data: KotlinEntityDataStore<Persistable> by lazy {
        // override onUpgrade to handle migrating to a new version
        val source = DatabaseSource(context, Models.DEFAULT, 1)

        if (BuildConfig.DEBUG) {
            // use this in development mode to drop and recreate the tables on every upgrade
            source.setTableCreationMode(TableCreationMode.DROP_CREATE)
        }

        val configuration = source.configuration
        KotlinEntityDataStore<Persistable>(configuration)
    }

    override fun putLink(request: CreateDeepLinkRequest): Long {
        val model = request.let { request ->
            RequeryDeepLinkInfoEntity().apply {
                this.deepLink = request.deepLink
                this.label = request.label
                this.updatedTime = request.updatedTime
                this.deepLinkHandlers = request.deepLinkHandlers
            }
        }

        return data.insert(model).id
    }

    override fun getLink(deepLinkId: Long): DeepLinkInfo? {
        return data.findByKey(RequeryDeepLinkInfoEntity::class, deepLinkId)
                ?.let { deepLinkInfoFromEntity(it) }
    }

    override fun removeLink(deepLinkId: Long) {
        // TODO surely there's a better way to do this...
        data.delete(data.findByKey(RequeryDeepLinkInfoEntity::class, deepLinkId) as RequeryDeepLinkInfoEntity)
    }

    override fun clearAllHistory() {
        data.delete().invoke()
    }

    override fun addListener(listener: DeepLinkDatabase.Listener): Int {
        val id = Random().nextInt()
        listeners.put(id, listener)
        notifyListener(id)
        return id
    }

    override fun containsLink(deepLink: Uri): Boolean {
        return data.select(RequeryDeepLinkInfoEntity::class)
                .where(RequeryDeepLinkInfoEntity.DEEP_LINK.eq(deepLink))
                .limit(1)
                .get()
                .any()
    }

    override fun removeListener(id: Int) {
        listeners.remove(id)
    }

    private fun notifyListeners() {
        val dataSnapshot = data.select(RequeryDeepLinkInfoEntity::class).get().map { deepLinkInfoFromEntity(it) }
        listeners.values.forEach { it.onDataChanged(dataSnapshot) }
    }

    private fun notifyListener(id: Int) = listeners[id]?.onDataChanged(data.select(RequeryDeepLinkInfoEntity::class).get().map {
        deepLinkInfoFromEntity(it)
    })

    private fun deepLinkInfoFromEntity(it: RequeryDeepLinkInfoEntity) = DeepLinkInfo(it.id, it.deepLink, it.label, it.updatedTime, it.deepLinkHandlers)
}
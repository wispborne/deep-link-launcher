package com.thunderclouddev.deeplink.data.requery

import android.content.Context
import android.net.Uri
import com.thunderclouddev.deeplink.BuildConfig
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.data.DeepLinkDatabase
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.kotlin.invoke
import io.requery.query.Result
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode
import java.util.*

/**
 * Implementation of [DeepLinkDatabase] using Requery.
 *
 * @author David Whitman on 28 Jan, 2017.
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
        val existingItem: RequeryDeepLinkInfoEntity? = data.select(RequeryDeepLinkInfoEntity::class)
                .where(RequeryDeepLinkInfoEntity.DEEP_LINK.eq(request.deepLink))
                .limit(1)
                .get()
                .firstOrNull()

        val id = if (existingItem != null) {
            data.update(existingItem.apply {
                this.deepLink = request.deepLink
                this.deepLinkHandlers = request.deepLinkHandlers
                this.label = request.label
                this.updatedTime = request.updatedTime
            }).id
        } else {
            val model = request.toModel()
            data.insert(model).id
        }

        notifyListeners()
        return id
    }

    override fun getLink(deepLinkId: Long): DeepLinkInfo? {
        return data.findByKey(RequeryDeepLinkInfoEntity::class, deepLinkId)?.toDeepLinkInfo()
    }

    override fun removeLink(deepLinkId: Long) {
        val result = data.delete(RequeryDeepLinkInfoEntity::class)
                .where(RequeryDeepLinkInfoEntity.ID.eq(deepLinkId))
                .invoke()
                .value()
        TimberKt.v { "Deleted item $result" }
        notifyListeners()
    }

    override fun clearAllHistory() {
        data.delete().invoke()
        notifyListeners()
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
        val dataSnapshot: Result<RequeryDeepLinkInfoEntity>? = data.select(RequeryDeepLinkInfoEntity::class).get()
        val mapped = dataSnapshot?.map { it.toDeepLinkInfo() } ?: emptyList()
        listeners.values.forEach { it.onDataChanged(mapped) }
    }

    private fun notifyListener(id: Int): Unit? {
        val dataSnapshot: Result<RequeryDeepLinkInfoEntity>? = data.select(RequeryDeepLinkInfoEntity::class).get()
        val mapped = dataSnapshot?.map { it.toDeepLinkInfo() }

        return listeners[id]?.onDataChanged(mapped ?: emptyList())
    }

    private fun RequeryDeepLinkInfoEntity.toDeepLinkInfo() = DeepLinkInfo(this.id, this.deepLink, this.label, this.updatedTime, this.deepLinkHandlers)

    private fun CreateDeepLinkRequest.toModel() = RequeryDeepLinkInfoEntity().apply {
        deepLink = this@toModel.deepLink
        label = this@toModel.label
        updatedTime = this@toModel.updatedTime
        deepLinkHandlers = this@toModel.deepLinkHandlers
    }
}
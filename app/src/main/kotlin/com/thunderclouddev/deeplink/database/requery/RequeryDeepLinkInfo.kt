package com.thunderclouddev.deeplink.database.requery

import android.net.Uri
import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable

@Entity
interface RequeryDeepLinkInfo : Persistable {
    @get:Key
    @get:Generated
    var id: Long
    var deepLink: Uri
    var label: String?
    var updatedTime: Long
    var deepLinkHandlers: List<String>
}
package com.thunderclouddev.deeplink.data.requery

import android.net.Uri
import io.requery.*
import io.requery.android.UriConverter

@Entity
interface RequeryDeepLinkInfo : Persistable {
    @get:Key
    @get:Generated
    var id: Long

    @get:Index
    @get:Convert(UriConverter::class)
    var deepLink: Uri

    var label: String?

    var updatedTime: Long

    @get:Convert(StringListConverter::class)
    var deepLinkHandlers: List<String>
}
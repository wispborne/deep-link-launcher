package com.thunderclouddev.deeplink.data.requery

import io.requery.*

@Entity
interface RequeryDeepLinkInfo : Persistable {
    @get:Key
    @get:Generated
    var id: Long

    @get:Index
    var deepLink: String

    var label: String?

    var updatedTime: Long

    @get:Convert(StringListConverter::class)
    var deepLinkHandlers: List<String>
}
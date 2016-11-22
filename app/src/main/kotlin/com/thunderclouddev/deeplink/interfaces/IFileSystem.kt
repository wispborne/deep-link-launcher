package com.thunderclouddev.deeplink.interfaces

interface IFileSystem {
    fun write(key: String, value: String)

    fun read(key: String): String?

    fun clear(key: String)

    fun clearAll()

    fun keyList(): List<String>

    fun values(): List<String>

    fun all(): Map<String, String>
}

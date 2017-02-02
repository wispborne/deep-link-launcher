package com.thunderclouddev.deeplink.data

import android.content.Context
import android.content.SharedPreferences
import com.thunderclouddev.deeplink.data.FileSystem
import java.util.*

class SharedPrefsFileSystem(context: Context, key: String) : FileSystem {
    private val preferences: SharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)

    override fun write(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun read(key: String): String? {
        return preferences.getString(key, null)
    }

    override fun clear(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun clearAll() {
        preferences.edit().clear().apply()
    }

    override fun keyList(): List<String> {
        return ArrayList(preferences.all.keys)
    }

    override fun values() = keyList().map { read(it) }.filterNotNull()

    override fun all() = preferences.all.filterValues { it != null }.mapValues { it.value.toString() }
}
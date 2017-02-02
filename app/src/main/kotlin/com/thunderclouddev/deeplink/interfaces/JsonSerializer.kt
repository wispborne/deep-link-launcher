package com.thunderclouddev.deeplink.interfaces

import android.net.Uri
import com.google.gson.GsonBuilder
import com.thunderclouddev.deeplink.utils.UriGsonAdapter
import javax.inject.Inject

/**
 * Created by David Whitman on 01 Feb, 2017.
 */
interface JsonSerializer {
    fun toJson(obj: Any?): String
    fun <T> fromJson(jsonString: String?, clazz: Class<T>): T?
}

class GsonSerializer @Inject constructor() : JsonSerializer {
    private val gson = GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriGsonAdapter()).create()

    override fun toJson(obj: Any?): String = gson.toJson(obj)
    override fun <T> fromJson(jsonString: String?, clazz: Class<T>): T? {
        try {
            return gson.fromJson(jsonString, clazz)
        } catch (exception: Exception) {
            return null
        }

    }
}
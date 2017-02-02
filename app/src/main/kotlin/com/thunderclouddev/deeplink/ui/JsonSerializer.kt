package com.thunderclouddev.deeplink.ui

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type
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

    /**
     * [https://gist.github.com/logcat/3399e60132c1f2f9c261]
     */
    private class UriGsonAdapter : com.google.gson.JsonSerializer<Uri>, JsonDeserializer<Uri> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Uri {
            return Uri.parse(json.asString)
        }

        override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }
    }
}

package com.thunderclouddev.deeplink.utils


import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type

/**
 * [https://gist.github.com/logcat/3399e60132c1f2f9c261]
 */
class UriGsonAdapter : JsonSerializer<Uri>, JsonDeserializer<Uri> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Uri {
        return Uri.parse(json.asString)
    }

    override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }
}
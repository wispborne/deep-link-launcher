package com.thunderclouddev.deeplink.database

import android.net.Uri
import android.util.Log
import com.thunderclouddev.deeplink.empty
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import org.json.JSONException
import org.json.JSONObject

/**
 * Serialize and deserialize [DeepLinkInfo] to/from Json.
 * Created by David Whitman on 21 Jan, 2017.
 */
class DeepLinkInfoJsonSerializer() {
    val KEY_SCHEMA_VERSION = "schema_version"
    val KEY_ID = "id"
    val KEY_DEEP_LINK = "deep_link"
    val KEY_LINK_HANDLERS = "link_handlers"
    val KEY_ACTIVITY_LABEL = "label"
    val KEY_UPDATED_TIME = "update_time"

    fun toJson(deepLinkInfo: DeepLinkInfo, schemaVersion: Int): Long {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(KEY_SCHEMA_VERSION, schemaVersion)
            jsonObject.put(KEY_ID, deepLinkInfo.id)
            jsonObject.put(KEY_DEEP_LINK, deepLinkInfo.deepLink)
            jsonObject.put(KEY_ACTIVITY_LABEL, deepLinkInfo.label)
            jsonObject.put(KEY_UPDATED_TIME, deepLinkInfo.updatedTime)
            jsonObject.put(KEY_LINK_HANDLERS, deepLinkInfo.deepLinkHandlers)
            return deepLinkInfo.id
        } catch (jsonException: JSONException) {
            TimberKt.e(jsonException, { "Failed to write deep link with item=$deepLinkInfo" })
            return deepLinkInfo.id
        }
    }

    fun fromJson(deepLinkJson: String): DeepLinkInfo? {
        Log.d("deeplink", "json string = " + deepLinkJson)

        try {
            val jsonObject = JSONObject(deepLinkJson)

            val schemaVersion = parseField<Int>(jsonObject, KEY_SCHEMA_VERSION) ?: 0

            val id = parseField<Long>(jsonObject, KEY_ID) ?: 0
            val deepLink = parseField<String>(jsonObject, KEY_DEEP_LINK) ?: String.empty
            val activityLabel = parseField<String>(jsonObject, KEY_ACTIVITY_LABEL) ?: String.empty
            val handlers = parseField<Array<String>>(jsonObject, KEY_LINK_HANDLERS)?.toMutableList() ?: mutableListOf()
            val updatedTime = parseField<Long>(jsonObject, KEY_UPDATED_TIME) ?: 0

            // Migration
            if (schemaVersion < 3) {
                val packageName = if (schemaVersion < 1)
                    parseField<String>(jsonObject, "pacakage_name") ?: String.empty
                else parseField<String>(jsonObject, "package_name") ?: String.empty
                handlers.add(packageName)
            }

            return DeepLinkInfo(id, Uri.parse(deepLink), activityLabel, updatedTime, handlers)
        } catch (exception: Exception) {
            TimberKt.e(exception, { "Failed to parse deep link entirely with json: $deepLinkJson" })
            return null
        }
    }

    private fun <T> parseField(jsonObject: JSONObject, key: String): T? {
        try {
            return jsonObject.get(key) as T
        } catch (exception: Exception) {
            when (exception) {
                is JSONException,
                is ClassCastException -> {
                    TimberKt.d(exception, {
                        "Failed to parse deep link property with key=$key " +
                                "and value=${if (jsonObject.has(key)) jsonObject[key] else String.empty} " +
                                "and jsonObject=$jsonObject "
                    })
                }
            }

            return null
        }
    }
}
package com.thunderclouddev.deeplink.database

import android.net.Uri
import android.util.Log
import com.thunderclouddev.deeplink.empty
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import org.json.JSONException
import org.json.JSONObject

/**
 * Serialize and deserialize [DeepLinkInfo] to/from Json.
 * Created by David Whitman on 21 Jan, 2017.
 */
class DeepLinkInfoSerializer() {
    val KEY_SCHEMA_VERSION = "schema_version"
    val KEY_DEEP_LINK = "deep_link"
    val KEY_PACKAGE_NAME = "package_name"
    val KEY_ACTIVITY_LABEL = "label"
    val KEY_UPDATED_TIME = "update_time"

    fun toJson(deepLinkInfo: DeepLinkInfo, schemaVersion: Int): String {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(KEY_SCHEMA_VERSION, schemaVersion)
            jsonObject.put(KEY_DEEP_LINK, deepLinkInfo.deepLink)
            jsonObject.put(KEY_ACTIVITY_LABEL, deepLinkInfo.label)
            jsonObject.put(KEY_PACKAGE_NAME, deepLinkInfo.packageName)
            jsonObject.put(KEY_UPDATED_TIME, deepLinkInfo.updatedTime)
            return jsonObject.toString()
        } catch (jsonException: JSONException) {
            return deepLinkInfo.id
        }
    }

    fun fromJson(deepLinkJson: String): DeepLinkInfo? {
        Log.d("deeplink", "json string = " + deepLinkJson)

        try {
            val jsonObject = JSONObject(deepLinkJson)

            val schemaVersion = parseField<Int>(jsonObject, KEY_SCHEMA_VERSION) ?: 0

            val deepLink = parseField<String>(jsonObject, KEY_DEEP_LINK) ?: String.empty
            val activityLabel = parseField<String>(jsonObject, KEY_ACTIVITY_LABEL) ?: String.empty
            var packageName = parseField<String>(jsonObject, KEY_PACKAGE_NAME) ?: String.empty
            val updatedTime = parseField<Long>(jsonObject, KEY_UPDATED_TIME) ?: 0

            if (schemaVersion < 1) {
                packageName = parseField<String>(jsonObject, "pacakage_name") ?: String.empty
            }

            return DeepLinkInfo(Uri.parse(deepLink), activityLabel, packageName, updatedTime)
        } catch (jsonException: JSONException) {
            Log.d("deeplink", "returning null for deep link info, exception = " + jsonException)
            return null
        }
    }

    private fun <T> parseField(jsonObject: JSONObject, key: String): T? {
        try {
            return jsonObject.get(key) as T
        } catch (ignored: JSONException) {
            return null
        }
    }
}
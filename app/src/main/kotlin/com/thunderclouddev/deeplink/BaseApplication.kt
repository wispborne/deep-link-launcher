package com.thunderclouddev.deeplink

import android.app.Application
import android.net.Uri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.GsonBuilder
import com.thunderclouddev.deeplink.database.requery.RequeryDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistory
import com.thunderclouddev.deeplink.utils.UriGsonAdapter
import hotchemi.android.rate.AppRate


open class BaseApplication : Application() {
    companion object {
        // Should probably implement dagger at some point.
        lateinit var deepLinkHistory: DeepLinkHistory
    }

    object Json {
        private val jsonSerializer = GsonBuilder()
                .registerTypeAdapter(Uri::class.java, UriGsonAdapter()).create()

        fun toJson(obj: Any?): String = jsonSerializer.toJson(obj)
        fun <T> fromJson(jsonString: String?, clazz: Class<T>): T? {
            try {
                return jsonSerializer.fromJson(jsonString, clazz)
            } catch (exception: Exception) {
                return null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        deepLinkHistory = DeepLinkHistory(createDatabase())

        val arePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS

        if (arePlayServicesAvailable) {
            AppRate.with(this)
                    .setInstallDays(4) //number of days since install, default 10
                    .setLaunchTimes(5) //number of minimum launches, default 10
                    .setShowNeverButton(true)
                    .setRemindInterval(2) //number of days since remind me later was clicked
                    .monitor()
        }
    }

    protected open fun createDatabase() = RequeryDatabase(this)
//    protected open fun createDatabase() = SharedPrefsDeepLinkDatabase(this)
}

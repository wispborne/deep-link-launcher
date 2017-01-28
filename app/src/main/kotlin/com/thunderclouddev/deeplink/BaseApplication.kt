package com.thunderclouddev.deeplink

import android.app.Application
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.thunderclouddev.deeplink.database.requery.RequeryDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistory
import hotchemi.android.rate.AppRate
import org.greenrobot.eventbus.EventBus


open class BaseApplication : Application() {
    companion object {
        lateinit var deepLinkHistory: DeepLinkHistory

        // Should probably implement dagger at some point.
        val bus = EventBus()
    }

    object Json {
        fun toJson(obj: Any?): String = jsonMapper.writeValueAsString(obj)
        fun <T> fromJson(jsonString: String?, clazz: Class<T>): T? {
            try {
                return jsonMapper.readValue(jsonString, clazz)
            } catch (exception: Exception) {
                return null
            }
        }

        private val jsonMapper = ObjectMapper().registerModule(KotlinModule())
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

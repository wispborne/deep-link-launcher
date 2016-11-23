package com.thunderclouddev.deeplink

import android.app.Application
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.database.SharedPrefsDeepLinkDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistoryFeature
import hotchemi.android.rate.AppRate

open class BaseApplication : Application() {
    companion object {
        lateinit var database: DeepLinkDatabase
    }

    override fun onCreate() {
        super.onCreate()

        database = SharedPrefsDeepLinkDatabase(this)

        DeepLinkHistoryFeature.getInstance(applicationContext)

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
}

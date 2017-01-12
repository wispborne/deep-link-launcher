package com.thunderclouddev.deeplink

import android.app.Application
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.thunderclouddev.deeplink.database.SharedPrefsDeepLinkDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistory
import hotchemi.android.rate.AppRate
import org.greenrobot.eventbus.EventBus

open class BaseApplication : Application() {
    companion object {
        lateinit var deepLinkHistory: DeepLinkHistory

        val bus = EventBus()
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

    protected open fun createDatabase() = SharedPrefsDeepLinkDatabase(this)
}

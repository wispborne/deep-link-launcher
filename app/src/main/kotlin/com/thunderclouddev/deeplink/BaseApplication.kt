package com.thunderclouddev.deeplink

import android.app.Application
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.database.SharedPrefsDeepLinkDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistoryFeature
import com.thunderclouddev.deeplink.utils.Utilities

open class BaseApplication : Application() {
    companion object {
        lateinit var database: DeepLinkDatabase
    }

    override fun onCreate() {
        super.onCreate()

        database = SharedPrefsDeepLinkDatabase(this)

        DeepLinkHistoryFeature.getInstance(applicationContext)
        Utilities.initializeAppRateDialog(applicationContext)
    }
}

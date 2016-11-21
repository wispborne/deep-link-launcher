package com.thunderclouddev.deeplink

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistoryFeature
import com.thunderclouddev.deeplink.features.LinkQueueHandler
import com.thunderclouddev.deeplink.utils.Utilities

open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Constants.isFirebaseAvailable(this)) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            LinkQueueHandler.getInstance(applicationContext).runQueueListener()
        }

        DeepLinkHistoryFeature.getInstance(applicationContext)
        Utilities.initializeAppRateDialog(applicationContext)
    }
}

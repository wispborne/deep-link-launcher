package com.manoj.dlt

import android.app.Application
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.google.firebase.database.FirebaseDatabase
import com.manoj.dlt.features.DeepLinkHistoryFeature
import com.manoj.dlt.features.LinkQueueHandler
import com.manoj.dlt.features.ProfileFeature
import com.manoj.dlt.utils.Utilities
import io.fabric.sdk.android.Fabric

class DeepLinkTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Constants.ENVIRONMENT == Constants.CONFIG.PRODUCTION) {
            Fabric.with(this, Crashlytics())
            Crashlytics.setUserIdentifier(ProfileFeature.getInstance(this).userId)
            Crashlytics.setString("user id", ProfileFeature.getInstance(this).userId)
        } else {
            Toast.makeText(applicationContext, "In Testing mode", Toast.LENGTH_LONG).show()
        }

        if (Constants.isFirebaseAvailable(this)) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            LinkQueueHandler.getInstance(applicationContext).runQueueListener()
        }

        DeepLinkHistoryFeature.getInstance(applicationContext)
        Utilities.initializeAppRateDialog(applicationContext)
    }
}

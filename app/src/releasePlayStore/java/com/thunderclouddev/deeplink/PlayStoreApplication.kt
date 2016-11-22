package com.thunderclouddev.deeplink

import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.database.FirebaseDatabase
import com.thunderclouddev.deeplink.database.FirebaseDeepLinkDatabase
import com.thunderclouddev.deeplink.features.LinkQueueHandler
import com.thunderclouddev.deeplink.features.ProfileFeature
import com.thunderclouddev.deeplink.logging.timber.CrashlyticsTree
import com.thunderclouddev.deeplink.logging.timber.Timber
import io.fabric.sdk.android.Fabric

/**
 * Created by David Whitman on 21 Nov, 2016.
 */
class PlayStoreApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()

        val arePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS

        if (arePlayServicesAvailable) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            LinkQueueHandler.getInstance(applicationContext).runQueueListener()

            database = FirebaseDeepLinkDatabase(this)
        }

        Fabric.with(this, Crashlytics())
        Crashlytics.setUserIdentifier(ProfileFeature.getInstance(this).userId)
        Crashlytics.setString("user id", ProfileFeature.getInstance(this).userId)

        Timber.plant(CrashlyticsTree())

    }
}
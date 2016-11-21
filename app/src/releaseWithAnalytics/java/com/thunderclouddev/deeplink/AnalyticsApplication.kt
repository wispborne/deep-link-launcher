package com.thunderclouddev.deeplink

import com.crashlytics.android.Crashlytics
import com.thunderclouddev.deeplink.features.ProfileFeature
import com.thunderclouddev.deeplink.logging.timber.CrashlyticsTree
import com.thunderclouddev.deeplink.logging.timber.Timber
import io.fabric.sdk.android.Fabric


/**
 * Created by David Whitman on 21 Nov, 2016.
 */
class AnalyticsApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())
        Crashlytics.setUserIdentifier(ProfileFeature.getInstance(this).userId)
        Crashlytics.setString("user id", ProfileFeature.getInstance(this).userId)

        Timber.plant(CrashlyticsTree())
    }
}
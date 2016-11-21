package com.thunderclouddev.deeplink

import com.facebook.stetho.Stetho
import com.thunderclouddev.deeplink.logging.timber.Timber


/**
 * Created by David Whitman on 21 Nov, 2016.
 */
class DebugApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build())

        Timber.plant(Timber.DebugTree())
    }
}
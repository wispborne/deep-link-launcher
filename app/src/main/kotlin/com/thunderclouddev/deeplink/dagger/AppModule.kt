package com.thunderclouddev.deeplink.dagger

import android.app.Application
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.database.requery.RequeryDatabase
import com.thunderclouddev.deeplink.features.DeepLinkHistory
import com.thunderclouddev.deeplink.features.DeepLinkLauncher
import com.thunderclouddev.deeplink.interfaces.GsonSerializer
import com.thunderclouddev.deeplink.interfaces.IDeepLinkHistory
import com.thunderclouddev.deeplink.interfaces.JsonSerializer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author David Whitman on 31 Jan, 2017.
 */
@Module
class AppModule(private val context: Application) {

    @Provides
    @Singleton
    fun provideDeepLinkHistory(database: DeepLinkDatabase): IDeepLinkHistory = DeepLinkHistory(database)

    @Provides
    @Singleton
    fun provideJsonSerializer(): JsonSerializer = GsonSerializer()

    @Provides
    @Singleton
    fun provideDeepLinkLauncher(deepLinkHistory: IDeepLinkHistory) = DeepLinkLauncher(deepLinkHistory)

    @Provides
    @Singleton
    fun provideDatabase(): DeepLinkDatabase = RequeryDatabase(context)
}
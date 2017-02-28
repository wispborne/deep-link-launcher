package com.thunderclouddev.deeplink.dagger

import android.app.Application
import com.thunderclouddev.deeplink.data.DeepLinkDatabase
import com.thunderclouddev.deeplink.data.DeepLinkHistory
import com.thunderclouddev.deeplink.data.requery.RequeryDatabase
import com.thunderclouddev.deeplink.ui.DeepLinkLauncher
import com.thunderclouddev.deeplink.ui.GsonSerializer
import com.thunderclouddev.deeplink.ui.JsonSerializer
import com.thunderclouddev.deeplink.ui.edit.HandlingAppsForUriFactory
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
    fun provideDeepLinkHistory(database: DeepLinkDatabase): DeepLinkHistory = DeepLinkHistory(database)

    @Provides
    @Singleton
    fun provideJsonSerializer(): JsonSerializer = GsonSerializer()

    @Provides
    @Singleton
    fun provideDeepLinkLauncher(deepLinkHistory: DeepLinkHistory): DeepLinkLauncher = DeepLinkLauncher(deepLinkHistory)

    @Provides
    @Singleton
    fun provideDatabase(): DeepLinkDatabase = RequeryDatabase(context)

    @Provides
    fun provideHandlingAppsForUriFactory() = HandlingAppsForUriFactory(context)
}
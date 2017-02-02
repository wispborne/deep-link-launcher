package com.thunderclouddev.deeplink.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.thunderclouddev.deeplink.data.DeepLinkHistory
import com.thunderclouddev.deeplink.utils.hasAnyHandlingActivity
import com.thunderclouddev.deeplink.utils.Utilities

/**
 * @author David Whitman on 01 Feb, 2017.
 */
class DeepLinkLauncher(private val deepLinkHistory: DeepLinkHistory) {
    fun resolveAndFire(deepLinkUri: Uri, context: Context): Boolean {
        val intent = Utilities.createDeepLinkIntent(deepLinkUri)

        return if (intent.hasAnyHandlingActivity(context.packageManager)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            if (!deepLinkHistory.containsLink(deepLinkUri)) {
                val deepLinkRequest = Utilities.createDeepLinkRequest(deepLinkUri, context.packageManager)
                deepLinkHistory.addLink(deepLinkRequest)
            }

            true
        } else false
    }
}
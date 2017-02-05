package com.thunderclouddev.deeplink.ui

import android.content.Context
import android.content.Intent
import com.thunderclouddev.deeplink.data.DeepLinkHistory
import com.thunderclouddev.deeplink.utils.Utilities
import com.thunderclouddev.deeplink.utils.hasAnyHandlingActivity
import com.thunderclouddev.deeplink.utils.isUri

/**
 * @author David Whitman on 01 Feb, 2017.
 */
class DeepLinkLauncher(private val deepLinkHistory: DeepLinkHistory) {
    fun resolveAndFire(deepLinkString: String, context: Context): Boolean {
        if (!deepLinkString.isUri()) {
            return false
        }

        val deepLinkUri = Uri.parse(deepLinkString)
        val intent = Utilities.createDeepLinkIntent(deepLinkUri)

        return if (intent.hasAnyHandlingActivity(context.packageManager)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            if (!deepLinkHistory.containsLink(deepLinkUri.toString())) {
                val deepLinkRequest = Utilities.createDeepLinkRequest(deepLinkUri, context.packageManager)
                deepLinkHistory.addLink(deepLinkRequest)
            }

            true
        } else false
    }
}
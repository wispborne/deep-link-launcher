package com.thunderclouddev.deeplink.ui

import android.content.Context
import android.content.Intent
import com.thunderclouddev.deeplink.data.DeepLinkHistory
import com.thunderclouddev.deeplink.utils.Utilities
import com.thunderclouddev.deeplink.utils.asUri
import com.thunderclouddev.deeplink.utils.hasAnyHandlingActivity

/**
 * Attempts to launch a deep link for a given [Uri]. If successful, adds the link to the history.
 *
 * @author David Whitman on 01 Feb, 2017.
 */
class DeepLinkLauncher(private val deepLinkHistory: DeepLinkHistory) {
    /**
     * Tries to launch the deep link. Returns true if successful, false otherwise.
     */
    fun resolveAndFire(deepLinkString: String, context: Context): Boolean {
        val deepLinkUri = deepLinkString.asUri()

        return deepLinkUri?.let {
            val intent = Utilities.createDeepLinkIntent(deepLinkUri)

            if (intent.hasAnyHandlingActivity(context.packageManager)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)

                if (!deepLinkHistory.containsLink(deepLinkUri.toString())) {
                    val deepLinkRequest = Utilities.createDeepLinkRequest(deepLinkUri, context.packageManager)
                    deepLinkHistory.addLink(deepLinkRequest)
                }

                true
            } else false
        } ?: false
    }
}
package com.thunderclouddev.deeplink.events

import android.net.Uri

/**
 * Created by David Whitman on 23 Jan, 2017.
 */
data class DeepLinkLaunchFailedEvent(val attemptedDeepLink: String, val reason: FAILURE_REASON) {
    enum class FAILURE_REASON {
        NO_ACTIVITY_FOUND,
        IMPROPER_URI,
        UNKNOWN
    }
}
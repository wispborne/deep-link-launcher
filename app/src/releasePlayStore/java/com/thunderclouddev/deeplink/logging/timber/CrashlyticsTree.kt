package com.thunderclouddev.deeplink.logging.timber

import android.util.Log
import com.crashlytics.android.Crashlytics

/**
 * Created by David Whitman on 21 Nov, 2016.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority >= Log.ERROR) {
            if (t == null) {
                if (message != null) {
                    Crashlytics.log(message)
                }
            } else {
                Crashlytics.logException(t)
            }
        }
    }
}
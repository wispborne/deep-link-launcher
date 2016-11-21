package com.thunderclouddev.deeplink

import android.content.Context

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object Constants {
    enum class CONFIG {
        DEBUG, RELEASE, RELEASE_WITH_ANALYTICS
    }

    val DEEP_LINK_HISTORY_KEY = "deep_link_history_key_v1"
    val GLOBAL_PREF_KEY = "one_time_key"
    val APP_TUTORIAL_SEEN = "app_tut_seen"
    val SHORTCUT_HINT_SEEN = "shortcut_hint_seen"
    val USER_ID_KEY = "user_id"
    val GOOGLE_PLAY_URI = "https://play.google.com/store/apps/details?id=" // TODO fill this id

    var ENVIRONMENT: CONFIG = when (BuildConfig.BUILD_TYPE) {
        "release" -> CONFIG.RELEASE
        "releaseWithAnalytics" -> CONFIG.RELEASE_WITH_ANALYTICS
        else -> CONFIG.DEBUG
    }

    val firebaseUserRef: DatabaseReference
        get() = FirebaseDatabase.getInstance()
                .getReference(ENVIRONMENT.name.toLowerCase())
                .child(DbConstants.USERS)

    fun isFirebaseAvailable(context: Context): Boolean {
        val playServicesAvl = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        return playServicesAvl
    }
}

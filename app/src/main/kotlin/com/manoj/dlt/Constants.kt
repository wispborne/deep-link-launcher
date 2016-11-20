package com.manoj.dlt

import android.content.Context

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object Constants {
    enum class CONFIG {
        SANDBOX, PRODUCTION
    }

    val DEEP_LINK_HISTORY_KEY = "deep_link_history_key_v1"
    val GLOBAL_PREF_KEY = "one_time_key"
    val APP_TUTORIAL_SEEN = "app_tut_seen"
    val SHORTCUT_HINT_SEEN = "shortcut_hint_seen"
    val USER_ID_KEY = "user_id"
    val GOOGLE_PLAY_URI = "https://play.google.com/store/apps/details?id=com.manoj.dlt"
    var ENVIRONMENT: CONFIG = BuildConfig.CONFIG


    val firebaseUserRef: DatabaseReference
        get() = FirebaseDatabase.getInstance()
                .getReference(ENVIRONMENT.name.toLowerCase())
                .child(DbConstants.USERS)

    fun isFirebaseAvailable(context: Context): Boolean {
        val playServicesAvl = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return playServicesAvl == ConnectionResult.SUCCESS
    }
}

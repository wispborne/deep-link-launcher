package com.thunderclouddev.deeplink

object Constants {
    enum class CONFIG {
        DEBUG, RELEASE, RELEASE_PLAY_STORE
    }

    val DEEP_LINK_HISTORY_KEY = "deep_link_history_key_v1"
    val GLOBAL_PREF_KEY = "one_time_key"
    val APP_TUTORIAL_SEEN = "app_tut_seen"
    val SHORTCUT_HINT_SEEN = "shortcut_hint_seen"
    val USER_ID_KEY = "user_id"
    val GOOGLE_PLAY_URI = "https://play.google.com/store/apps/details?id=" // TODO fill this id

    var ENVIRONMENT: CONFIG = when (BuildConfig.BUILD_TYPE) {
        "release" -> CONFIG.RELEASE
        "releasePlayStore" -> CONFIG.RELEASE_PLAY_STORE
        else -> CONFIG.DEBUG
    }
}

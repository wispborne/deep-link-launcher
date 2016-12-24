package com.thunderclouddev.deeplink

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri


fun Throwable.hasCause(type: Class<*>): Boolean {
    var cause = this

    while (cause.cause != null) {
        cause = cause.cause!!

        if (cause.javaClass.name.equals(type.name)) {
            return true
        }
    }

    return false
}

fun Intent?.hasHandlingActivity(packageManager: PackageManager) =
        if (this == null) false else packageManager.queryIntentActivities(this, 0).isNotEmpty()

val Any?.simpleClassName: String
    get() = this?.javaClass?.simpleName ?: String.empty

@Suppress("unused")
val String.Companion.empty: String
    get() = ""

val String.getOrNullIfBlank: String?
    get() = if (this.isNullOrBlank()) null else this


fun CharSequence?.isNotNullOrBlank() = !this.isNullOrBlank()


fun String?.isUri() = this != null
        && Uri.parse(this).scheme.isNotNullOrBlank()
        && !this.contains("\n")
        && !this.contains(" ")
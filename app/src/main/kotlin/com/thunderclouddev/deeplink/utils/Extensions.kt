package com.thunderclouddev.deeplink.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.view.View


fun Throwable.hasCause(type: Class<*>): Boolean {
    var cause = this

    while (cause.cause != null) {
        cause = cause.cause!!

        if (cause.javaClass.name == type.name) {
            return true
        }
    }

    return false
}

fun Intent?.hasAnyHandlingActivity(packageManager: PackageManager) =
        if (this == null) false else packageManager.queryIntentActivities(this, 0).isNotEmpty()

fun Intent?.handlingActivities(packageManager: PackageManager): List<ResolveInfo> =
        if (this == null) emptyList<ResolveInfo>() else packageManager.queryIntentActivities(this, 0)

val Any?.simpleClassName: String
    get() = this?.javaClass?.simpleName ?: String.empty

@Suppress("unused")
val String.Companion.empty: String
    get() = ""

fun String?.getOrNullIfBlank() = if (this.isNullOrBlank()) null else this

fun CharSequence?.isNotNullOrBlank() = !this.isNullOrBlank()

fun CharSequence.allIndicesOf(terms: Collection<String>): MutableList<Int> {
    val indices = mutableListOf<Int>()

    terms.forEach { term ->
        var index = this.indexOf(term)
        while (index >= 0) {
            indices.add(index)
            index = this.indexOf(term, index + 1)
        }
    }

    return indices
}

val Boolean.visibleOrGone: Int
    get() = if (this) View.VISIBLE else View.GONE

var View.showing: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = value.visibleOrGone
    }

fun String?.isUri() = this != null
        && Uri.parse(this).scheme.isNotNullOrBlank()
        && !this.contains("\n")
        && !this.contains(" ")
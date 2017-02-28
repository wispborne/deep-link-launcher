package com.thunderclouddev.deeplink.ui.edit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.thunderclouddev.deeplink.ui.Uri
import javax.inject.Inject


/**
 * @author David Whitman on 28 Feb, 2017.
 */
class HandlingAppsForUriFactory @Inject constructor(private val context: Context) {

    fun build(uri: Uri, defaultOnly: Boolean = true): List<AppViewModel> {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri.toString()))
        val defaultFlag = if (defaultOnly) PackageManager.MATCH_DEFAULT_ONLY else 0
        val packageManager = context.packageManager
        return packageManager.queryIntentActivities(intent, defaultFlag)
                .map {
                    AppViewModel(it.activityInfo.packageName,
                            it.activityInfo.loadLabel(packageManager).toString(),
                            getBitmapFromDrawable(packageManager.getApplicationIcon(it.activityInfo.packageName)))
                }
    }

    private fun getBitmapFromDrawable(drawable: Drawable) = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
    }
}
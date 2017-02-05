package com.thunderclouddev.deeplink.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AlertDialog
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.data.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import com.thunderclouddev.deeplink.ui.Uri

object Utilities {
    fun addShortcut(deepLink: DeepLinkInfo, context: Context, shortcutName: String): Boolean {
        if (!deepLink.deepLink.isUri()) {
            return false
        }

        val shortcutIntent = createDeepLinkIntent(Uri.parse(deepLink.deepLink))
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName)

        try {
            val icon = context.packageManager.getApplicationIcon(deepLink.deepLinkHandlers.firstOrNull())
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, (icon as BitmapDrawable).bitmap)
            intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            context.sendBroadcast(intent)
            return true
        } catch (exception: Exception) {
            TimberKt.e(exception, { exception.message!! })
            return false
        }

    }

    fun createDeepLinkIntent(deepLinkUri: Uri): Intent {
        val intent = Intent()
        intent.data = android.net.Uri.parse(deepLinkUri.toString())
        intent.action = Intent.ACTION_VIEW
        return intent
    }

    fun createDeepLinkRequest(deepLink: Uri, packageManager: PackageManager): CreateDeepLinkRequest {
        return CreateDeepLinkRequest(String.empty, null, System.currentTimeMillis(),
                createDeepLinkIntent(deepLink)
                        .handlingActivities(packageManager)
                        .map { it.activityInfo.packageName ?: String.empty })
    }

    fun raiseError(errorText: String, context: Context) {
        showAlert(context.getString(R.string.error_title), errorText, context)
        TimberKt.e(Exception(errorText), { errorText })
    }

    fun showAlert(title: String, message: String, context: Context) {
        AlertDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .show()
    }
}

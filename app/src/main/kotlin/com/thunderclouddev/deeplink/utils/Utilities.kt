package com.thunderclouddev.deeplink.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.thunderclouddev.deeplink.BaseApplication
import com.thunderclouddev.deeplink.Constants
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.events.DeepLinkFireEvent
import com.thunderclouddev.deeplink.features.FileSystem
import com.thunderclouddev.deeplink.isUri
import com.thunderclouddev.deeplink.logging.Timber
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType

object Utilities {
    fun checkAndFireDeepLink(deepLinkUri: String, context: Context): Boolean {
        if (deepLinkUri.isUri()) {
            val uri = Uri.parse(deepLinkUri)

            if (resolveAndFire(uri, context)) {
                return true
            } else {
                val deepLinkInfo = DeepLinkInfo(uri, "", "", -1)
                val deepLinkFireEvent = DeepLinkFireEvent(ResultType.FAILURE, deepLinkInfo, DeepLinkFireEvent.FAILURE_REASON.NO_ACTIVITY_FOUND)
                BaseApplication.bus.postSticky(deepLinkFireEvent)
                return false
            }
        } else {
            val deepLinkInfo = DeepLinkInfo(Uri.EMPTY, "", "", -1)
            val deepLinkFireEvent = DeepLinkFireEvent(ResultType.FAILURE, deepLinkInfo, DeepLinkFireEvent.FAILURE_REASON.IMPROPER_URI)
            BaseApplication.bus.postSticky(deepLinkFireEvent)
            return false
        }
    }

    fun addShortcut(deepLink: DeepLinkInfo, context: Context, shortcutName: String): Boolean {
        val shortcutIntent = createDeepLinkIntent(deepLink.deepLink)
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName)

        try {
            val icon = context.packageManager.getApplicationIcon(deepLink.packageName)
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, (icon as BitmapDrawable).bitmap)
            intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            context.sendBroadcast(intent)
            return true
        } catch (exception: Exception) {
            Timber.e(exception, { exception.message!! })
            return false
        }

    }

    fun resolveAndFire(deepLinkUri: Uri, context: Context): Boolean {
        val intent = createDeepLinkIntent(deepLinkUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resolveInfo = getResolveInfo(context, intent)

        return if (resolveInfo != null) {
            context.startActivity(intent)
            val deepLinkInfo = createDeepLinkInfo(deepLinkUri, resolveInfo)
            val deepLinkFireEvent = DeepLinkFireEvent(ResultType.SUCCESS, deepLinkInfo)
            BaseApplication.bus.postSticky(deepLinkFireEvent)
            true
        } else {
            false
        }
    }

    fun createDeepLinkIntent(deepLinkUri: Uri): Intent {
        val intent = Intent()
        intent.data = deepLinkUri
        intent.action = Intent.ACTION_VIEW
        return intent
    }

    fun raiseError(errorText: String, context: Context) {
        showAlert(context.getString(R.string.error_title), errorText, context)
        Timber.e(Exception(errorText), { errorText })
    }

    fun showAlert(title: String, message: String, context: Context) {
        AlertDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .show()
    }

    fun createDeepLinkInfo(deepLink: Uri, context: Context): DeepLinkInfo? {
        val resolveInfo = getResolveInfo(context, createDeepLinkIntent(deepLink))
        return if (resolveInfo != null)
            createDeepLinkInfo(deepLink, resolveInfo)
        else
            null
    }

    private fun createDeepLinkInfo(deepLink: Uri, resolveInfo: ResolveInfo): DeepLinkInfo {
        val packageName = resolveInfo.activityInfo.packageName
        return DeepLinkInfo(deepLink, null, packageName, System.currentTimeMillis())
    }

    private fun getResolveInfo(context: Context, intent: Intent): ResolveInfo? {
        return context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }

    fun isAppTutorialSeen(context: Context): Boolean {
        val tutSeenBool = getOneTimeStore(context).read(Constants.APP_TUTORIAL_SEEN)
        return tutSeenBool != null && tutSeenBool == "true"
    }

    fun getOneTimeStore(context: Context): FileSystem {
        return FileSystem(context, Constants.GLOBAL_PREF_KEY)
    }

    fun setAppTutorialSeen(seen: Boolean, context: Context) {
        getOneTimeStore(context).write(Constants.APP_TUTORIAL_SEEN, if (seen) "true" else "false")
    }

    fun shareApp(context: Context) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
        sendIntent.type = "text/plain"
        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title)))
    }
}

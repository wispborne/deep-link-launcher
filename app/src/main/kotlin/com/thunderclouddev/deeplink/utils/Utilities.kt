package com.thunderclouddev.deeplink.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.thunderclouddev.deeplink.*
import com.thunderclouddev.deeplink.events.DeepLinkLaunchFailedEvent
import com.thunderclouddev.deeplink.events.DeepLinkLaunchedEvent
import com.thunderclouddev.deeplink.features.FileSystem
import com.thunderclouddev.deeplink.logging.timberkt.Timber
import com.thunderclouddev.deeplink.models.CreateDeepLinkRequest
import com.thunderclouddev.deeplink.models.DeepLinkInfo

object Utilities {
    fun checkAndFireDeepLink(deepLink: String, context: Context): Boolean {
        if (deepLink.isUri()) {
            val uri = Uri.parse(deepLink)

            if (resolveAndFire(uri, context)) {
                return true
            } else {
                val deepLinkFireEvent = DeepLinkLaunchFailedEvent(uri.toString(), DeepLinkLaunchFailedEvent.FAILURE_REASON.NO_ACTIVITY_FOUND)
                BaseApplication.bus.postSticky(deepLinkFireEvent)
                return false
            }
        } else {
            val deepLinkFireEvent = DeepLinkLaunchFailedEvent(deepLink, DeepLinkLaunchFailedEvent.FAILURE_REASON.IMPROPER_URI)
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
            val icon = context.packageManager.getApplicationIcon(deepLink.deepLinkHandlers.firstOrNull())
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

        return if (intent.hasHandlingActivity(context.packageManager)) {
            context.startActivity(intent)
            val deepLinkInfo = createDeepLinkRequest(deepLinkUri, context.packageManager)
            val deepLinkFireEvent = DeepLinkLaunchedEvent(deepLinkInfo)
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

    fun createDeepLinkRequest(deepLink: Uri, packageManager: PackageManager): CreateDeepLinkRequest {
        return CreateDeepLinkRequest(deepLink, null, System.currentTimeMillis(),
                createDeepLinkIntent(deepLink).handlingActivities(packageManager)
                        .map { it.resolvePackageName ?: String.empty })
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

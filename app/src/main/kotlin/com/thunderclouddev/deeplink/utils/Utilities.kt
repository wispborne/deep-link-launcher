package com.thunderclouddev.deeplink.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.thunderclouddev.deeplink.Constants
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.events.DeepLinkFireEvent
import com.thunderclouddev.deeplink.features.FileSystem
import com.thunderclouddev.deeplink.isUri
import com.thunderclouddev.deeplink.logging.Timber
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType
import org.greenrobot.eventbus.EventBus

object Utilities {
    fun checkAndFireDeepLink(deepLinkUri: String, context: Context): Boolean {
        if (deepLinkUri.isUri()) {
            val uri = Uri.parse(deepLinkUri)

            if (resolveAndFire(uri, context)) {
                return true
            } else {
                val deepLinkInfo = DeepLinkInfo(uri, "", "", -1)
                val deepLinkFireEvent = DeepLinkFireEvent(ResultType.FAILURE, deepLinkInfo, DeepLinkFireEvent.FAILURE_REASON.NO_ACTIVITY_FOUND)
                EventBus.getDefault().postSticky(deepLinkFireEvent)
                return false
            }
        } else {
            val deepLinkInfo = DeepLinkInfo(Uri.EMPTY, "", "", -1)
            val deepLinkFireEvent = DeepLinkFireEvent(ResultType.FAILURE, deepLinkInfo, DeepLinkFireEvent.FAILURE_REASON.IMPROPER_URI)
            EventBus.getDefault().postSticky(deepLinkFireEvent)
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
            val deepLinkInfo = createDeepLinkInfo(deepLinkUri, resolveInfo, context)
            val deepLinkFireEvent = DeepLinkFireEvent(ResultType.SUCCESS, deepLinkInfo)
            EventBus.getDefault().postSticky(deepLinkFireEvent)
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

    fun colorPartialString(text: String, startPos: Int, length: Int, color: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val spannable = SpannableString(text)
        spannable.setSpan(ForegroundColorSpan(color), startPos, startPos + length, 0)
        builder.append(spannable)
        return builder
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
            createDeepLinkInfo(deepLink, resolveInfo, context)
        else
            null
    }

    private fun createDeepLinkInfo(deepLink: Uri, resolveInfo: ResolveInfo, context: Context): DeepLinkInfo {
        val packageName = resolveInfo.activityInfo.packageName
        val activityLabel = resolveInfo.loadLabel(context.packageManager).toString()
        return DeepLinkInfo(deepLink, activityLabel, packageName, System.currentTimeMillis())
    }

    private fun getResolveInfo(context: Context, intent: Intent): ResolveInfo? {
        return context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }

    fun isAppTutorialSeen(context: Context): Boolean {
        val tutSeenBool = getOneTimeStore(context).read(Constants.APP_TUTORIAL_SEEN)
        return tutSeenBool != null && tutSeenBool == "true"
    }

    fun isShortcutHintSeen(context: Context): Boolean {
        val shortcutSeenString = getOneTimeStore(context).read(Constants.SHORTCUT_HINT_SEEN)
        return shortcutSeenString != null && shortcutSeenString == "true"
    }

    fun getOneTimeStore(context: Context): FileSystem {
        return FileSystem(context, Constants.GLOBAL_PREF_KEY)
    }

    fun setAppTutorialSeen(seen: Boolean, context: Context) {
        getOneTimeStore(context).write(Constants.APP_TUTORIAL_SEEN, if (seen) "true" else "false")
    }

    fun setShortcutBannerSeen(context: Context) {
        getOneTimeStore(context).write(Constants.SHORTCUT_HINT_SEEN, "true")
    }

    fun showKeyboard(activityContext: Context) {
        val imm = activityContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun hideKeyboard(viewInWindow: View) {
        val windowContext = viewInWindow.context
        val imm = windowContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(viewInWindow.windowToken, 0)
    }

    fun shareApp(context: Context) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
        sendIntent.type = "text/plain"
        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title)))
    }
}

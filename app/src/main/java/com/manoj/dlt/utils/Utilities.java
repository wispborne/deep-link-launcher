package com.manoj.dlt.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.manoj.dlt.Constants;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.R;
import com.manoj.dlt.events.DeepLinkFireEvent;
import com.manoj.dlt.features.FileSystem;
import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.models.ResultType;

import org.greenrobot.eventbus.EventBus;

import hotchemi.android.rate.AppRate;

public class Utilities
{
    public static boolean checkAndFireDeepLink(String deepLinkUri, Context context)
    {
        if(isProperUri(deepLinkUri))
        {
            if(resolveAndFire(deepLinkUri, context))
            {
                return true;
            } else
            {
                DeepLinkInfo deepLinkInfo = new DeepLinkInfo(deepLinkUri, null, null, -1);
                DeepLinkFireEvent deepLinkFireEvent = new DeepLinkFireEvent(ResultType.FAILURE, deepLinkInfo, DeepLinkFireEvent.FAILURE_REASON.NO_ACTIVITY_FOUND);
                EventBus.getDefault().postSticky(deepLinkFireEvent);
                return false;
            }
        } else
        {
            DeepLinkInfo deepLinkInfo = new DeepLinkInfo(deepLinkUri, null, null, -1);
            DeepLinkFireEvent deepLinkFireEvent = new DeepLinkFireEvent(ResultType.FAILURE, deepLinkInfo, DeepLinkFireEvent.FAILURE_REASON.IMPROPER_URI);
            EventBus.getDefault().postSticky(deepLinkFireEvent);
            return false;
        }
    }

    public static boolean addShortcut(String deepLinkUri, Context context, String shortcutName)
    {
        final Intent shortcutIntent = getDeepLinkIntent(deepLinkUri);
        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        // Set the custom shortcut icon. Not sure about this, but seems to work
        ResolveInfo resolveInfo = getResolveInfo(context, getDeepLinkIntent(deepLinkUri));
        try
        {
            Drawable icon = context.getPackageManager().getApplicationIcon(resolveInfo.activityInfo.packageName);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, ((BitmapDrawable)icon).getBitmap());
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            context.sendBroadcast(intent);
            return true;
        } catch (Exception exception)
        {
            Crashlytics.logException(exception);
            exception.printStackTrace();
            return false;
        }
    }

    public static boolean isProperUri(String uriText)
    {
        Uri uri = Uri.parse(uriText);
        if (uri.getScheme() == null || uri.getScheme().length() == 0)
        {
            return false;
        } else if (uriText.contains("\n") || uriText.contains(" "))
        {
            return false;
        } else
        {
            return true;
        }
    }

    public static boolean resolveAndFire(String deepLinkUri, Context context)
    {
        Intent intent = getDeepLinkIntent(deepLinkUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ResolveInfo resolveInfo = getResolveInfo(context, intent);
        if (resolveInfo != null)
        {
            context.startActivity(intent);
            DeepLinkInfo deepLinkInfo = getDeepLinkInfo(deepLinkUri, resolveInfo, context);
            DeepLinkFireEvent deepLinkFireEvent = new DeepLinkFireEvent(ResultType.SUCCESS, deepLinkInfo);
            EventBus.getDefault().postSticky(deepLinkFireEvent);
            return true;
        } else
        {
            return false;
        }
    }

    private static ResolveInfo getResolveInfo(Context context, Intent intent)
    {
        PackageManager pm = context.getPackageManager();
        return pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    private static Intent getDeepLinkIntent(String deepLinkUri) {
        Uri uri = Uri.parse(deepLinkUri);
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        return intent;
    }

    public static SpannableStringBuilder colorPartialString(String text, int startPos, int length, int color)
    {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(color), startPos, startPos + length, 0);
        builder.append(spannable);
        return builder;
    }

    public static void raiseError(String errorText, Context context)
    {
        showAlert(context.getString(R.string.error_title), errorText, context);
        Crashlytics.logException(new Exception(errorText));
    }

    public static void showAlert(String title, String message, Context context)
    {
        new AlertDialog.Builder(context).
                setTitle(title)
                .setMessage(message)
                .show();
    }

    public static void setTextViewText(View ancestor, int textViewId, CharSequence text)
    {
        ((TextView) ancestor.findViewById(textViewId)).setText(text);
    }

    public static DeepLinkInfo getDeepLinkInfo(String deepLink, ResolveInfo resolveInfo, Context context)
    {
        String packageName = resolveInfo.activityInfo.packageName;
        String activityLabel = resolveInfo.loadLabel(context.getPackageManager()).toString();
        DeepLinkInfo deepLinkInfo = new DeepLinkInfo(deepLink, activityLabel, packageName, System.currentTimeMillis());
        return deepLinkInfo;
    }

    public static boolean isAppTutorialSeen(Context context)
    {
        String tutSeenBool = getOneTimeStore(context).read(Constants.APP_TUTORIAL_SEEN);
        if (tutSeenBool != null && tutSeenBool.equals("true"))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static boolean isShortcutHintSeen(Context context)
    {
        String shortcutSeenString = getOneTimeStore(context).read(Constants.SHORTCUT_HINT_SEEN);
        if (shortcutSeenString != null && shortcutSeenString.equals("true"))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static FileSystem getOneTimeStore(Context context)
    {
        return new FileSystem(context, Constants.GLOBAL_PREF_KEY);
    }

    public static void setAppTutorialSeen(Context context)
    {
        getOneTimeStore(context).write(Constants.APP_TUTORIAL_SEEN, "true");
    }

    public static void setShortcutBannerSeen(Context context)
    {
        getOneTimeStore(context).write(Constants.SHORTCUT_HINT_SEEN, "true");
    }

    public static void showKeyboard(Context activityContext)
    {
        InputMethodManager imm = (InputMethodManager) activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(View viewInWindow)
    {
        Context windowContext = viewInWindow.getContext();
        InputMethodManager imm = (InputMethodManager) windowContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(viewInWindow.getWindowToken(), 0);
    }

    public static void initializeAppRateDialog(Context context)
    {
        AppRate.with(context)
                .setInstallDays(0) //number of days since install, default 10
                .setLaunchTimes(3) //number of minimum launches, default 10
                .setShowNeverButton(false)
                .setRemindInterval(2) //number of days since remind me later was clicked
                .monitor();
    }

    public static DeepLinkInfo getLinkInfo(DataSnapshot dataSnapshot)
    {
        long updatedTime = Long.parseLong(dataSnapshot.child(DbConstants.DL_UPDATED_TIME).getValue().toString());
        return new DeepLinkInfo(dataSnapshot.child(DbConstants.DL_DEEP_LINK).getValue().toString(),
                dataSnapshot.child(DbConstants.DL_ACTIVITY_LABEL).getValue().toString(),
                dataSnapshot.child(DbConstants.DL_PACKAGE_NAME).getValue().toString(),
                updatedTime);
    }

    public static void shareApp(Context context)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text));
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title)));
    }

}

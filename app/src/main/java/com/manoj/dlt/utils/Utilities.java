package com.manoj.dlt.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.manoj.dlt.Constants;
import com.manoj.dlt.R;
import com.manoj.dlt.features.DeepLinkHistoryFeature;
import com.manoj.dlt.features.FileSystem;
import com.manoj.dlt.models.DeepLinkInfo;
import hotchemi.android.rate.AppRate;

public class Utilities
{
    public static boolean checkAndFireDeepLink(String deepLinkUri, Context context)
    {
        if(isProperUri(deepLinkUri))
        {
            if(resolveAndAddToHistory(deepLinkUri, context))
            {
                return true;
            } else
            {
                raiseError(context.getString(R.string.error_no_activity_resolved).concat(": ").concat(deepLinkUri), context);
                return false;
            }
        } else
        {
            raiseError(context.getString(R.string.error_improper_uri).concat(": ").concat(deepLinkUri), context);
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

    public static boolean resolveAndAddToHistory(String deepLinkUri, Context context)
    {
        Uri uri = Uri.parse(deepLinkUri);
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm = context.getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null)
        {
            context.startActivity(intent);
            Utilities.addResolvedInfoToHistory(deepLinkUri, resolveInfo, context);
            return true;
        } else
        {
            return false;
        }
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
        new AlertDialog.Builder(context).
                setTitle(context.getString(R.string.error_title))
                .setMessage(errorText)
                .show();
        Crashlytics.logException(new Exception(errorText));
    }

    public static void setTextViewText(View ancestor, int textViewId, CharSequence text)
    {
        ((TextView) ancestor.findViewById(textViewId)).setText(text);
    }

    public static void addResolvedInfoToHistory(String deepLink, ResolveInfo resolveInfo, Context context)
    {
        String packageName = resolveInfo.activityInfo.packageName;
        String activityLabel = resolveInfo.loadLabel(context.getPackageManager()).toString();
        DeepLinkInfo deepLinkInfo = new DeepLinkInfo(deepLink, activityLabel, packageName, System.currentTimeMillis());
        DeepLinkHistoryFeature.getInstance(context).addLinkToHistory(deepLinkInfo);
    }

    public static boolean isAppTutorialSeen(Context context)
    {
        FileSystem oneTimeBooleanStore = new FileSystem(context, Constants.ONE_TIME_PREF_KEY);
        String tutSeenBool = oneTimeBooleanStore.read(Constants.APP_TUTORIAL_SEEN);
        if (tutSeenBool != null && tutSeenBool.equals("true"))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static void setAppTutorialSeen(Context context)
    {
        FileSystem oneTimeBooleanStore = new FileSystem(context, Constants.ONE_TIME_PREF_KEY);
        oneTimeBooleanStore.write(Constants.APP_TUTORIAL_SEEN, "true");
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

}

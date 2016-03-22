package com.manoj.dlt.utils;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import com.manoj.dlt.Constants;
import com.manoj.dlt.features.DeepLinkHistory;
import com.manoj.dlt.features.FileSystem;
import com.manoj.dlt.models.DeepLinkInfo;

public class Utilities
{
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
        Toast.makeText(context, errorText, Toast.LENGTH_LONG).show();
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
        new DeepLinkHistory(context).addLinkToHistory(deepLinkInfo);
    }

    public static boolean isAppTutorialSeen(Context context)
    {
        FileSystem oneTimeBooleanStore =  new FileSystem(context, Constants.ONE_TIME_PREF_KEY);
        String tutSeenBool = oneTimeBooleanStore.read(Constants.APP_TUTORIAL_SEEN);
        if(tutSeenBool != null && tutSeenBool.equals("true"))
        {
            return true;
        }else
        {
            return false;
        }
    }

    public static void setAppTutorialSeen(Context context)
    {
        FileSystem oneTimeBooleanStore =  new FileSystem(context, Constants.ONE_TIME_PREF_KEY);
        oneTimeBooleanStore.write(Constants.APP_TUTORIAL_SEEN, "true");
    }

    public static void showKeyboard(Context activityContext)
    {
        InputMethodManager imm = (InputMethodManager) activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public static void hideKeyboard(View viewInWindow)
    {
        Context windowContext = viewInWindow.getContext();
        InputMethodManager imm = (InputMethodManager) windowContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(viewInWindow.getWindowToken(),0);
    }

}

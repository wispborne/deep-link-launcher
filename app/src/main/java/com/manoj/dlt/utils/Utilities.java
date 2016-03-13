package com.manoj.dlt.utils;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.manoj.dlt.features.DeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;

public class Utilities
{
    public static SpannableStringBuilder colorPartialString(String text, int startPos, int endPos, int color)
    {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString redSpannable = new SpannableString(text);
        redSpannable.setSpan(new ForegroundColorSpan(color), startPos, endPos, 0);
        builder.append(redSpannable);
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

}

package com.manoj.dlt;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

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
}

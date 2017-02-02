package com.thunderclouddev.deeplink.ui

import android.content.Context
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.utils.allIndicesOf
import com.thunderclouddev.deeplink.utils.isNotNullOrBlank
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import com.thunderclouddev.deeplink.utils.ColorUtils

/**
 * Colorizes a deep link, returning it as a [SpannableString].
 *
 * Created by David Whitman on 20 Jan, 2017.
 */
class DeepLinkColorizer(val colors: Colors) {
    constructor(context: Context) : this(createDefaultColors(context))

    /**
     * Colorizes a deep link, returning it as a [SpannableString].
     */
    fun colorize(deepLink: String): SpannableString {
        try {
            val uri = Uri.parse(deepLink)
            val spannable = SpannableString(deepLink)

            spannable.setSpan(ForegroundColorSpan(colors.schemeColor), 0, uri.scheme.length, 0)

            if (uri.host.isNotNullOrBlank()) {
                val indexOfHost = deepLink.indexOf(uri.host)
                spannable.setSpan(ForegroundColorSpan(colors.hostColor), indexOfHost, indexOfHost + uri.host.length, 0)
            }

            if (uri.path.isNotNullOrBlank()) {
                val indexOfPath = deepLink.indexOf(uri.path)
                spannable.setSpan(ForegroundColorSpan(colors.pathColor), indexOfPath + 1, indexOfPath + uri.encodedPath.length, 0)
            }

            if (uri.query.isNotNullOrBlank()) {
                var alternatePathColorsSwitch = true
                var lastIndexSearched = 0

                uri.queryParameterNames.forEach { queryName ->
                    val queryParamValue = uri.getQueryParameter(queryName)
                    val color = if (alternatePathColorsSwitch) colors.queryColor1 else colors.queryColor2
                    val indexOfQueryParam = deepLink.indexOf(queryName, lastIndexSearched)
                    val lengthOfQueryParam = Uri.encode(queryName).length + Uri.encode(queryParamValue).length + 1
                    spannable.setSpan(ForegroundColorSpan(color), indexOfQueryParam, indexOfQueryParam + lengthOfQueryParam, 0)
                    alternatePathColorsSwitch = !alternatePathColorsSwitch
                    lastIndexSearched = indexOfQueryParam + lengthOfQueryParam
                }
            }

            deepLink.allIndicesOf(arrayListOf(":", "/", "?", "&"))
                    .forEach { index ->
                        spannable.setSpan(ForegroundColorSpan(colors.symbolColor), index, index + 1, 0)
                    }

            return spannable
        } catch (ex: Exception) {
            TimberKt.w(ex, { "Error coloring $deepLink" })
            return SpannableString(deepLink)
        }
    }

    companion object {
        private fun createDefaultColors(context: Context) =
                Colors(schemeColor = ColorUtils.darken(ResourcesCompat.getColor(context.resources, R.color.grayDark, context.theme)),
                        symbolColor = ResourcesCompat.getColor(context.resources, R.color.grayDark, context.theme),
                        hostColor = ResourcesCompat.getColor(context.resources, R.color.primary, context.theme),
                        pathColor = ColorUtils.darken(ResourcesCompat.getColor(context.resources, R.color.grayDark, context.theme), .5),
                        queryColor1 = ResourcesCompat.getColor(context.resources, R.color.primary_dark, context.theme),
                        queryColor2 = ResourcesCompat.getColor(context.resources, R.color.primary_darker, context.theme))
    }


    data class Colors(val schemeColor: Int,
                      val hostColor: Int,
                      val pathColor: Int,
                      val queryColor1: Int,
                      val queryColor2: Int,
                      val symbolColor: Int)
}
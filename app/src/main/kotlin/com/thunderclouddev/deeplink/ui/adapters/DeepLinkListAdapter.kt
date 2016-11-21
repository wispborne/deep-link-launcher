package com.thunderclouddev.deeplink.ui.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.features.DeepLinkHistoryFeature
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.utils.Utilities
import java.util.*

class DeepLinkListAdapter(originalList: MutableList<DeepLinkInfo>, private val context: Context)
    : FilterableListAdapter<DeepLinkInfo>(originalList, false) {
    private val defaultAppIcon: Drawable
    private val titleColor: Int

    init {
        defaultAppIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_warning_red_24_px, context.theme)!!
        titleColor = ResourcesCompat.getColor(context.resources, R.color.primary, context.theme)
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, existingView: View?, viewGroup: ViewGroup): View {
        val convertView = existingView ?: LayoutInflater.from(context).inflate(R.layout.deep_link_info_layout, viewGroup, false)
        val deepLinkInfo = getItem(i)

        return createView(i, convertView, deepLinkInfo)
    }

    fun createView(position: Int, view: View, deepLinkInfo: DeepLinkInfo): View {
        val deepLink = deepLinkInfo.deepLink
        val deepLinkTitle = Utilities.colorPartialString(deepLink, deepLink.indexOf(searchString),
                searchString.length, titleColor)
        Utilities.setTextViewText(view, R.id.deep_link_title, deepLinkTitle)
        Utilities.setTextViewText(view, R.id.deep_link_package_name, deepLinkInfo.packageName)
        Utilities.setTextViewText(view, R.id.deep_link_activity_name, deepLinkInfo.activityLabel)
        try {
            val icon = context.packageManager.getApplicationIcon(deepLinkInfo.packageName)
            (view.findViewById(R.id.deep_link_icon) as ImageView).setImageDrawable(icon)
        } catch (exception: PackageManager.NameNotFoundException) {
            (view.findViewById(R.id.deep_link_icon) as ImageView).setImageDrawable(defaultAppIcon)
        }

        view.findViewById(R.id.deep_link_remove).setOnClickListener {
            originalList.removeAt(position)
            updateResults(searchString)
            DeepLinkHistoryFeature.getInstance(context).removeLinkFromHistory(deepLinkInfo.id)
        }
        return view
    }

    override fun getMatchingResults(constraint: CharSequence): List<DeepLinkInfo> {
        val prefixList = ArrayList<DeepLinkInfo>()
        val suffixList = ArrayList<DeepLinkInfo>()
        for (info in originalList) {
            if (info.deepLink.startsWith(constraint.toString())) {
                prefixList.add(info)
            } else if (info.deepLink.contains(constraint)) {
                suffixList.add(info)
            }
        }
        prefixList.addAll(suffixList)
        return prefixList
    }
}
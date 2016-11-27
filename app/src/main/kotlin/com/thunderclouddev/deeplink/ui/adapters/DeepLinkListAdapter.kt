package com.thunderclouddev.deeplink.ui.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.deepLinkListing.DeepLinkViewModel
import com.thunderclouddev.deeplink.ui.SortedListAdapter
import com.thunderclouddev.deeplink.utils.Utilities
import java.util.*

class DeepLinkListAdapter(context: Context, comparator: Comparator<DeepLinkViewModel>,
                          val menuItemListener: MenuItemListener) :
        SortedListAdapter<DeepLinkViewModel>(context, DeepLinkViewModel::class.java, comparator) {
    interface MenuItemListener {
        fun onMenuItemClick(menuItem: MenuItem, deepLinkViewModel: DeepLinkViewModel): Boolean
    }

    var stringToHighlight = ""

    private val defaultAppIcon: Drawable
    private val titleColor: Int

    init {
        defaultAppIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_warning_red_24_px, context.theme)!!
        titleColor = ResourcesCompat.getColor(context.resources, R.color.primary, context.theme)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.deep_link_info_layout, parent, false))
    }

    override fun areItemsTheSame(item1: DeepLinkViewModel, item2: DeepLinkViewModel)
            = item1.deepLinkInfo.id == item2.deepLinkInfo.id

    override fun areItemContentsTheSame(oldItem: DeepLinkViewModel, newItem: DeepLinkViewModel)
            = oldItem == newItem

    inner class ViewHolder(val view: View) :
            SortedListAdapter.ViewHolder<DeepLinkViewModel>(view) {

        override fun performBind(deepLinkInfoModel: DeepLinkViewModel) {
            val deepLinkInfo = deepLinkInfoModel.deepLinkInfo
            val deepLink = deepLinkInfo.deepLink
            val deepLinkTitle = Utilities.colorPartialString(deepLink, deepLink.indexOf(stringToHighlight),
                    stringToHighlight.length, titleColor)

            Utilities.setTextViewText(view, R.id.deepLinkItem_title, deepLinkTitle)
            Utilities.setTextViewText(view, R.id.deepLinkItem_packageName, deepLinkInfo.packageName)
            Utilities.setTextViewText(view, R.id.deepLinkItem_activityName, deepLinkInfo.activityLabel)

            try {
                val icon = view.context.packageManager.getApplicationIcon(deepLinkInfo.packageName)
                (view.findViewById(R.id.deepLinkItem_icon) as ImageView).setImageDrawable(icon)
            } catch (exception: PackageManager.NameNotFoundException) {
                (view.findViewById(R.id.deepLinkItem_icon) as ImageView).setImageDrawable(defaultAppIcon)
            }

            val overflowMenu = view.findViewById(R.id.deepLinkItem_overflow)
            overflowMenu.setOnClickListener {
                val menu = android.support.v7.widget.PopupMenu(view.context, overflowMenu)
                menu.setOnMenuItemClickListener { menuItemListener.onMenuItemClick(it, deepLinkInfoModel) }
                menu.inflate(R.menu.menu_list_item)

                menu.show()
            }
        }
    }
}
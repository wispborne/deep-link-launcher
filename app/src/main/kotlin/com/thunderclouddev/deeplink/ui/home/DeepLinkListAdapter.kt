package com.thunderclouddev.deeplink.ui.home

import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.databinding.DeepLinkInfoLayoutBinding
import com.thunderclouddev.deeplink.ui.SortedListAdapter
import com.thunderclouddev.deeplink.utils.Utilities
import com.thunderclouddev.deeplink.viewModels.DeepLinkViewModel
import java.util.*


class DeepLinkListAdapter(context: Context, comparator: Comparator<DeepLinkViewModel>,
                          val menuItemListener: MenuItemListener) :
        SortedListAdapter<DeepLinkViewModel>(context, DeepLinkViewModel::class.java, comparator) {
    interface MenuItemListener {
        fun onMenuItemClick(menuItem: MenuItem, deepLinkViewModel: DeepLinkViewModel): Boolean
    }

    var stringToHighlight = ""

    private val defaultAppIcon: Drawable = ResourcesCompat.getDrawable(context.resources,
            R.drawable.ic_warning_red_24_px, context.theme)!!
    private val titleColor: Int = ResourcesCompat.getColor(context.resources, R.color.primary,
            context.theme)

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<DeepLinkInfoLayoutBinding>(inflater, R.layout.deep_link_info_layout, parent, false))
    }

    override fun areItemsTheSame(item1: DeepLinkViewModel, item2: DeepLinkViewModel)
            = item1.deepLinkInfo.id.equals(item2.deepLinkInfo.id, ignoreCase = true)

    override fun areItemContentsTheSame(oldItem: DeepLinkViewModel, newItem: DeepLinkViewModel)
            = oldItem.deepLinkInfo.id.equals(newItem.deepLinkInfo.id, ignoreCase = true)

    inner class ViewHolder(val binding: DeepLinkInfoLayoutBinding) :
            SortedListAdapter.ViewHolder<DeepLinkViewModel>(binding) {

        override fun performBind(item: DeepLinkViewModel) {
            val deepLinkInfo = item.deepLinkInfo
            val deepLink = deepLinkInfo.deepLink
            val startPos = deepLink.toString().indexOf(stringToHighlight, ignoreCase = true)
            val deepLinkTitle = if (startPos >= 0) Utilities.colorPartialString(deepLink.toString(),
                    startPos,
                    stringToHighlight.length, titleColor) else deepLink.toString()

            binding.deepLinkItemTitle.text = deepLinkTitle
            binding.deepLinkItemPackageName.text = deepLinkInfo.packageName

            try {
                val icon = binding.root.context.packageManager.getApplicationIcon(deepLinkInfo.packageName)
                binding.deepLinkItemIcon.setImageDrawable(icon)
            } catch (exception: PackageManager.NameNotFoundException) {
                binding.deepLinkItemIcon.setImageDrawable(defaultAppIcon)
            }

            val overflowMenu = binding.deepLinkItemOverflow
            overflowMenu.setOnClickListener {
                val menu = android.support.v7.widget.PopupMenu(binding.root.context, overflowMenu)
                menu.setOnMenuItemClickListener { menuItemListener.onMenuItemClick(it, item) }
                menu.inflate(R.menu.menu_list_item)

                menu.show()
            }

//            view.findViewById(R.id.deepLinkItem_icon).setOnClickListener {
//                AlertDialog.Builder(view.context)
//                    .setView()
//            }
        }
    }
}
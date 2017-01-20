package com.thunderclouddev.deeplink.ui.home

import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.databinding.DeeplinkItemBinding
import com.thunderclouddev.deeplink.showing
import com.thunderclouddev.deeplink.ui.DeepLinkColorizer
import com.thunderclouddev.deeplink.ui.SortedListAdapter
import com.thunderclouddev.deeplink.viewModels.DeepLinkViewModel
import org.jetbrains.anko.AnkoContext
import java.util.*


class DeepLinkListAdapter(context: Context, comparator: Comparator<DeepLinkViewModel>,
                          val menuItemListener: DeepLinkListAdapter.MenuItemListener, val onItemClickListener: OnClickListener<DeepLinkViewModel>) :
        SortedListAdapter<DeepLinkViewModel>(DeepLinkViewModel::class.java, comparator) {
    interface MenuItemListener {
        fun onMenuItemClick(menuItem: MenuItem, deepLinkViewModel: DeepLinkViewModel): Boolean
    }

    var stringToHighlight = ""

    private val queryMatchHighlightColor: Int = ResourcesCompat.getColor(context.resources, R.color.accent, context.theme)
    private val defaultAppIcon: Drawable = ResourcesCompat.getDrawable(context.resources,
            R.drawable.ic_warning_red_24_px, context.theme)!!
    private val colorizer = DeepLinkColorizer(context)

    override fun areItemsTheSame(item1: DeepLinkViewModel, item2: DeepLinkViewModel)
            = item1.deepLinkInfo.id.equals(item2.deepLinkInfo.id, ignoreCase = true)

    override fun areItemContentsTheSame(oldItem: DeepLinkViewModel, newItem: DeepLinkViewModel)
            = oldItem.deepLinkInfo.id.equals(newItem.deepLinkInfo.id, ignoreCase = true)

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val view = ViewHolder(DataBindingUtil.inflate<DeeplinkItemBinding>(inflater, R.layout.deeplink_item, parent, false))
        val binding = view.binding

        val overflowMenu = binding.deepLinkItemOverflow
        val menu = android.support.v7.widget.PopupMenu(binding.root.context, overflowMenu)
        menu.inflate(R.menu.list_item)

        // Create anko class, then the view, then set the view's tag as the class so that we can change the onClick listener later
        val contextMenuBarView = ContextMenuBarView(menu.menu)
        val contextBarView = contextMenuBarView
                .createView(AnkoContext.Companion.create(binding.deepLinkItemContextMenu.context, binding.deepLinkItemContextMenu))
        contextBarView.tag = contextMenuBarView
        binding.deepLinkItemContextMenu.addView(contextBarView)

        return view
    }

    inner class ViewHolder(val binding: DeeplinkItemBinding) :
            SortedListAdapter.ViewHolder<DeepLinkViewModel>(binding) {

        override fun performBind(item: DeepLinkViewModel) {
            val deepLinkInfo = item.deepLinkInfo
            val deepLink = deepLinkInfo.deepLink
            val startPos = deepLink.toString().indexOf(stringToHighlight, ignoreCase = true)
            val deepLinkString = if (startPos >= 0) {
                val spannable = colorizer.colorize(deepLink.toString())
                highlightFilteredStringPart(spannable, startPos, stringToHighlight.length)
            } else {
                deepLink.toString()
            }

            binding.deepLinkItemTitle.text = deepLinkInfo.name ?: deepLinkInfo.packageName
            binding.deepLinkItemSubTitle.text = deepLinkString
            binding.deepLinkItemContextMenu.showing = item.showingContextMenu

            // Set icon
            try {
                val icon = binding.root.context.packageManager.getApplicationIcon(deepLinkInfo.packageName)
                binding.deepLinkItemIcon.setImageDrawable(icon)
            } catch (exception: PackageManager.NameNotFoundException) {
                binding.deepLinkItemIcon.setImageDrawable(defaultAppIcon)
            }

            // Set item click listener
            binding.deepLinkItemLayout.setOnClickListener {
                onItemClickListener.onItemClick(item)
            }

            // Set context menu click listener, both for overflow button and long-press
            val overflowMenu = binding.deepLinkItemOverflow
            overflowMenu.setOnClickListener {
                item.showingContextMenu = !item.showingContextMenu
                binding.deepLinkItemContextMenu.showing = item.showingContextMenu
            }
            binding.deepLinkItemLayout.setOnLongClickListener {
                item.showingContextMenu = !item.showingContextMenu
                binding.deepLinkItemContextMenu.showing = item.showingContextMenu
                true
            }

            // Hook up context menu's click listener to the adapter's menu click listener
            val contextMenuBarView = binding.deepLinkItemContextMenu.getChildAt(0).tag as ContextMenuBarView
            contextMenuBarView.onMenuItemClickListener =
                    MenuItem.OnMenuItemClickListener { menuItem ->
                        menuItemListener.onMenuItemClick(menuItem, item)
                    }
        }
    }

    private fun highlightFilteredStringPart(spannable: SpannableString, startPos: Int, length: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        spannable.setSpan(ForegroundColorSpan(queryMatchHighlightColor), startPos, startPos + length, 0)
        builder.append(spannable)
        return builder
    }
}
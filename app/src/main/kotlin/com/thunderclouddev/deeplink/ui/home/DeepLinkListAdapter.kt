package com.thunderclouddev.deeplink.ui.home

import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.allIndicesOf
import com.thunderclouddev.deeplink.databinding.DeeplinkItemBinding
import com.thunderclouddev.deeplink.isNotNullOrBlank
import com.thunderclouddev.deeplink.logging.Timber
import com.thunderclouddev.deeplink.ui.SortedListAdapter
import com.thunderclouddev.deeplink.ui.utils.ColorUtils
import com.thunderclouddev.deeplink.viewModels.DeepLinkViewModel
import java.util.*


class DeepLinkListAdapter(context: Context, comparator: Comparator<DeepLinkViewModel>,
                          val menuItemListener: MenuItemListener) :
        SortedListAdapter<DeepLinkViewModel>(context, DeepLinkViewModel::class.java, comparator) {
    interface MenuItemListener {
        fun onMenuItemClick(menuItem: MenuItem, deepLinkViewModel: DeepLinkViewModel): Boolean
    }

    var stringToHighlight = ""

    private val queryMatchHighlightColor: Int = ResourcesCompat.getColor(context.resources, R.color.accent, context.theme)

    private val schemeColor: Int = ColorUtils.darken(ResourcesCompat.getColor(context.resources, R.color.grayDark, context.theme), .5)
    private val symbolColor: Int = ResourcesCompat.getColor(context.resources, R.color.grayDark, context.theme)
    private val hostColor: Int = ResourcesCompat.getColor(context.resources, R.color.primary, context.theme)
    private val pathColor: Int = ResourcesCompat.getColor(context.resources, R.color.primary_darker, context.theme)
    private val query0Color: Int = ResourcesCompat.getColor(context.resources, R.color.accent, context.theme)
    private val query1Color: Int = ResourcesCompat.getColor(context.resources, R.color.accent_dark, context.theme)

    private val defaultAppIcon: Drawable = ResourcesCompat.getDrawable(context.resources,
            R.drawable.ic_warning_red_24_px, context.theme)!!

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<DeeplinkItemBinding>(inflater, R.layout.deeplink_item, parent, false))
    }

    override fun areItemsTheSame(item1: DeepLinkViewModel, item2: DeepLinkViewModel)
            = item1.deepLinkInfo.id.equals(item2.deepLinkInfo.id, ignoreCase = true)

    override fun areItemContentsTheSame(oldItem: DeepLinkViewModel, newItem: DeepLinkViewModel)
            = oldItem.deepLinkInfo.id.equals(newItem.deepLinkInfo.id, ignoreCase = true)

    inner class ViewHolder(val binding: DeeplinkItemBinding) :
            SortedListAdapter.ViewHolder<DeepLinkViewModel>(binding) {

        override fun performBind(item: DeepLinkViewModel) {
            val deepLinkInfo = item.deepLinkInfo
            val deepLink = deepLinkInfo.deepLink
            val startPos = deepLink.toString().indexOf(stringToHighlight, ignoreCase = true)
            val deepLinkTitle = if (startPos >= 0) {
                val spannable = colorCodeUri(deepLink.toString())
                highlightFilteredStringPart(spannable, startPos, stringToHighlight.length)
            } else {
                deepLink.toString()
            }

            binding.deepLinkItemTitle.text = deepLinkTitle
//            binding.deepLinkItemPackageName.text = deepLinkInfo.packageName

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
                menu.inflate(R.menu.list_item)

                menu.show()
            }

//            view.findViewById(R.id.deepLinkItem_icon).setOnClickListener {
//                AlertDialog.Builder(view.context)
//                    .setView()
//            }
        }

        private fun highlightFilteredStringPart(spannable: SpannableString, startPos: Int, length: Int): SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            spannable.setSpan(ForegroundColorSpan(queryMatchHighlightColor), startPos, startPos + length, 0)
            builder.append(spannable)
            return builder
        }

        private fun colorCodeUri(deepLink: String): SpannableString {
            try {
                val uri = Uri.parse(deepLink)
                val spannable = SpannableString(deepLink)

                spannable.setSpan(ForegroundColorSpan(schemeColor), 0, uri.scheme.length, 0)

                if (uri.host.isNotNullOrBlank()) {
                    val indexOfHost = deepLink.indexOf(uri.host)
                    spannable.setSpan(ForegroundColorSpan(hostColor), indexOfHost, indexOfHost + uri.host.length, 0)
                }

                if (uri.path.isNotNullOrBlank()) {
                    val indexOfPath = deepLink.indexOf(uri.path)
                    spannable.setSpan(ForegroundColorSpan(pathColor), indexOfPath + 1, indexOfPath + uri.path.length, 0)
                }

                if (uri.query.isNotNullOrBlank()) {
                    var alternatePathColorsSwitch = true
                    var lastIndexSearched = 0

                    uri.queryParameterNames.forEach { queryName ->
                        val queryParamValue = uri.getQueryParameter(queryName)
                        val color = if (alternatePathColorsSwitch) query0Color else query1Color
                        val indexOfQueryParam = deepLink.indexOf(queryName, lastIndexSearched)
                        val lengthOfQueryParam = queryName.length + queryParamValue.length + 1
                        spannable.setSpan(ForegroundColorSpan(color), indexOfQueryParam, indexOfQueryParam + lengthOfQueryParam, 0)
                        alternatePathColorsSwitch = !alternatePathColorsSwitch
                        lastIndexSearched = indexOfQueryParam + lengthOfQueryParam
                    }
                }

                deepLink.allIndicesOf(arrayListOf(":", "/", "?", "&"))
                        .forEach { index ->
                            spannable.setSpan(ForegroundColorSpan(symbolColor), index, index + 1, 0)
                        }

                return spannable
            } catch (ex: Exception) {
                Timber.w(ex, { "Error coloring $deepLink" })
                return SpannableString(deepLink)
            }
        }
    }
}
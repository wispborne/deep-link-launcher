package com.thunderclouddev.deeplink.ui.edit

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.view.ViewGroup
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.databinding.EditViewHandlingAppIconItemBinding
import com.thunderclouddev.deeplink.ui.BaseRecyclerViewAdapter
import org.jetbrains.anko.layoutInflater

/**
 * @author David Whitman on 27 Feb, 2017.
 */
class HandlingAppsAdapter(private val context: Context, val items: ObservableArrayList<AppViewModel>) : BaseRecyclerViewAdapter<HandlingAppsAdapter.ViewHolder>() {
    init {
        items.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<AppViewModel>>() {
            override fun onItemRangeInserted(p0: ObservableArrayList<AppViewModel>?, p1: Int, p2: Int) {
                notifyDataSetChanged()
            }

            override fun onChanged(p0: ObservableArrayList<AppViewModel>?) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(p0: ObservableArrayList<AppViewModel>?, p1: Int, p2: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeMoved(p0: ObservableArrayList<AppViewModel>?, p1: Int, p2: Int, p3: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(p0: ObservableArrayList<AppViewModel>?, p1: Int, p2: Int) {
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<EditViewHandlingAppIconItemBinding>(context.layoutInflater, R.layout.edit_view_handling_app_icon_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount() = items.size

    data class ViewHolder(val binding: EditViewHandlingAppIconItemBinding) : BaseRecyclerViewAdapter.ViewHolder<AppViewModel>(binding) {
        override fun performBind(item: AppViewModel) {
            binding.model = item
        }
    }
}
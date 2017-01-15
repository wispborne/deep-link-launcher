package com.thunderclouddev.deeplink.ui

import android.support.v7.widget.RecyclerView

/**
 * Created by David Whitman on 15 Jan, 2017.
 */
abstract class BaseRecyclerViewAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>() {
    interface OnClickListener<in T> {
        fun onItemClick(item: T)
    }
}
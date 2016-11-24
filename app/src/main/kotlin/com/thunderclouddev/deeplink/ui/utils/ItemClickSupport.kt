package com.thunderclouddev.deeplink.ui.utils

import android.support.v7.widget.RecyclerView
import android.view.View
import com.thunderclouddev.deeplink.R

/**
 * http://www.littlerobots.nl/blog/Handle-Android-RecyclerView-Clicks/
 */
class ItemClickSupport private constructor(private val recyclerView: RecyclerView) {
    private var onItemClickListener: OnItemClickListener? = null
    private var itemLongClickListener: OnItemLongClickListener? = null

    private val onClickListener = View.OnClickListener { v ->
        if (onItemClickListener != null) {
            val holder = recyclerView.getChildViewHolder(v)
            onItemClickListener!!.onItemClicked(recyclerView, holder.adapterPosition, v)
        }
    }

    private val onLongClickListener = View.OnLongClickListener { v ->
        if (itemLongClickListener != null) {
            val holder = recyclerView.getChildViewHolder(v)
            return@OnLongClickListener itemLongClickListener!!.onItemLongClicked(recyclerView, holder.adapterPosition, v)
        }
        false
    }

    private val attachListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            if (onItemClickListener != null) {
                view.setOnClickListener(onClickListener)
            }
            if (itemLongClickListener != null) {
                view.setOnLongClickListener(onLongClickListener)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {
        }
    }

    init {
        recyclerView.setTag(R.id.item_click_support, this)
        recyclerView.addOnChildAttachStateChangeListener(attachListener)
    }

    fun setOnItemClickListener(listener: OnItemClickListener): ItemClickSupport {
        onItemClickListener = listener
        return this
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener): ItemClickSupport {
        itemLongClickListener = listener
        return this
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(attachListener)
        view.setTag(R.id.item_click_support, null)
    }

    interface OnItemClickListener {
        fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean
    }

    companion object {
        fun addTo(view: RecyclerView): ItemClickSupport {
            var support: ItemClickSupport? = view.getTag(R.id.item_click_support) as ItemClickSupport?
            if (support == null) {
                support = ItemClickSupport(view)
            }
            return support
        }

        fun removeFrom(view: RecyclerView): ItemClickSupport {
            val support = view.getTag(R.id.item_click_support) as ItemClickSupport
            support.detach(view)
            return support
        }
    }
}

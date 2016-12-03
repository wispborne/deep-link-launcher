package com.thunderclouddev.deeplink.utils

import android.support.v7.util.SortedList

/**
 * Workaround for Android bug: https://code.google.com/p/android/issues/detail?id=201618
 * Warning: May be inefficient.
 */
class SafeSortedList<T> : SortedList<T> {
    private var callback: SortedList.Callback<T>

    constructor(klass: Class<T>, callback: SortedList.Callback<T>, initialCapacity: Int) : super(klass, callback, initialCapacity) {
        this.callback = callback
    }

    constructor(klass: Class<T>, callback: SortedList.Callback<T>) : super(klass, callback) {
        this.callback = callback
    }

    override fun add(item: T): Int {
        for (i in 0..size() - 1) {
            if (callback.areItemsTheSame(get(i), item)) {
                updateItemAt(i, item)
                recalculatePositionOfItemAt(i)
                return indexOf(item)
            }
        }

        return super.add(item)
    }

    override fun addAll(items: Array<T>, mayModifyInput: Boolean) = items.forEach { add(it) }

    override fun addAll(vararg items: T) = items.forEach { add(it) }

    override fun addAll(items: Collection<T>) = items.forEach { add(it) }
}
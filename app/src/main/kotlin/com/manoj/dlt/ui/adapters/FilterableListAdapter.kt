package com.manoj.dlt.ui.adapters

import android.util.Log
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import java.util.*

abstract class FilterableListAdapter<T>(protected var originalList: MutableList<T>, defaultListEmpty: Boolean) : BaseAdapter(), Filterable {
    protected var resultList = ArrayList<T>()
    protected var searchString = ""

    init {
        if (!defaultListEmpty) {
            resultList.addAll(originalList)
        }
    }

    override fun getCount(): Int {
        Log.d("deep", "size = " + resultList.size)
        return resultList.size
    }

    override fun getItem(i: Int): T {
        Log.d("deep", "call for item " + i)
        return resultList[i]
    }

    fun updateBaseData(baseData: List<T>) {
        originalList = baseData.toMutableList()
        updateResults(searchString)
    }

    fun updateResults(searchString: CharSequence) {
        filter.filter(searchString)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {
                val results = Filter.FilterResults()
                val resultList = getMatchingResults(charSequence)
                results.values = resultList
                results.count = resultList.size
                Log.d("deep", "filtering, count  = " + resultList.size)
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                searchString = charSequence.toString()
                resultList.clear()
                @Suppress("UNCHECKED_CAST")
                resultList.addAll(filterResults.values as List<T>)
                notifyDataSetChanged()
            }
        }
    }

    protected abstract fun getMatchingResults(constraint: CharSequence): List<T>
}

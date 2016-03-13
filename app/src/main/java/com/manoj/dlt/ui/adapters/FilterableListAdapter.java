package com.manoj.dlt.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterableListAdapter<T> extends BaseAdapter implements Filterable
{
    protected List<T> _resultList;
    protected List<T> _originalList;
    protected String _searchString;

    public FilterableListAdapter(List<T> originalList, boolean defaultListEmpty)
    {
        _resultList = new ArrayList<T>();
        if(!defaultListEmpty)
        {
            _resultList.addAll(originalList);
        }
        _originalList = originalList;
        _searchString = "";
    }

    @Override
    public int getCount()
    {
        return _resultList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return _resultList.get(i);
    }

    public void updateBaseData(List<T> baseData)
    {
        _originalList = baseData;
    }

    public void updateResults(CharSequence searchString)
    {
        getFilter().filter(searchString);
    }

    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();
                List<T> resultList = getMatchingResults(charSequence);
                results.values = resultList;
                results.count = resultList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                _searchString = charSequence.toString();
                _resultList = (List<T>) filterResults.values;
            }
        };
    }

    protected abstract List<T> getMatchingResults(CharSequence constraint);
}

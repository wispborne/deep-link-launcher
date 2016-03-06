package com.manoj.dlt.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.manoj.dlt.R;
import com.manoj.dlt.Utilities;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteMatchArrayAdapter extends ArrayAdapter<String>
{
    private String _searchString;
    private Context _context;
    private int _layoutId;
    private List<String> _stringList;

    public AutoCompleteMatchArrayAdapter(Context context, int layoutResourceId, List<String> stringList)
    {
        super(context, layoutResourceId, stringList);
        _context = context;
        _layoutId = layoutResourceId;
        _searchString = "";
        _stringList = stringList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String matchingString = getItem(position);
        if (convertView == null)
        {
            LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
            convertView = inflater.inflate(_layoutId, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text_view);
        int matchStartPos = matchingString.indexOf(_searchString);
        if (matchStartPos >= 0)
        {
            SpannableStringBuilder builder = Utilities.colorPartialString(matchingString, matchStartPos, matchStartPos + _searchString.length(), _context.getResources().getColor(R.color.Blue));
            textView.setText(builder, TextView.BufferType.SPANNABLE);
        } else
        {
            textView.setText(matchingString);
        }
        return convertView;
    }

    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults filterResults = new FilterResults();
                if (constraint != null)
                {
                    List<String> resultList = getMatchingStrings(constraint);
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                clear();
                if(constraint != null)
                {
                    _searchString = constraint.toString();
                }
                if (results != null && results.count > 0)
                {
                    addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(List<String> stringList)
    {
        _stringList = stringList;
        notifyDataSetChanged();
    }

    //Ordering of results are all prefix matches first, followed by all substring matches.
    private List<String> getMatchingStrings(CharSequence constraint)
    {
        ArrayList<String> prefixList = new ArrayList<>();
        ArrayList<String> substringList = new ArrayList<>();
        ArrayList<String> resultList = new ArrayList<>();
        for (String string : _stringList)
        {
            if (string.startsWith(constraint.toString()))
            {
                prefixList.add(string);
            }
            else if(string.contains(constraint))
            {
                substringList.add(string);
            }
        }
        resultList.addAll(prefixList);
        resultList.addAll(substringList);
        return resultList;
    }

}

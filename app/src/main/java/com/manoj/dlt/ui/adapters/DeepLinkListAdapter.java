package com.manoj.dlt.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import com.manoj.dlt.models.DeepLinkInfo;

import java.util.ArrayList;
import java.util.List;

public class DeepLinkListAdapter extends FilterableListAdapter<DeepLinkInfo>
{
    public DeepLinkListAdapter(List<DeepLinkInfo> originalList)
    {
        super(originalList, false);
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        return null;
    }

    @Override
    protected List<DeepLinkInfo> getMatchingResults(CharSequence constraint)
    {
        List<DeepLinkInfo> prefixList = new ArrayList<DeepLinkInfo>();
        List<DeepLinkInfo> suffixList = new ArrayList<DeepLinkInfo>();
        for(DeepLinkInfo info: _originalList)
        {
            if(info.getDeepLink().startsWith(constraint.toString()))
            {
                prefixList.add(info);
            }
            else if(info.getDeepLink().contains(constraint))
            {
                suffixList.add(info);
            }
        }
        prefixList.addAll(suffixList);
        return prefixList;
    }
}

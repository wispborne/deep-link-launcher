package com.manoj.dlt.ui.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.manoj.dlt.R;
import com.manoj.dlt.features.DeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class DeepLinkListAdapter extends FilterableListAdapter<DeepLinkInfo>
{
    private Context _context;

    public DeepLinkListAdapter(List<DeepLinkInfo> originalList, Context context)
    {
        super(originalList, false);
        _context = context;
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.deep_link_info_layout, viewGroup, false);
        }
        final DeepLinkInfo deepLinkInfo = (DeepLinkInfo) getItem(i);
        String deepLink = deepLinkInfo.getDeepLink();
        CharSequence deepLinkTitle = Utilities.colorPartialString(deepLink, deepLink.indexOf(_searchString), _searchString.length(), _context.getResources().getColor(R.color.Blue));
        Utilities.setTextViewText(convertView, R.id.deep_link_title, deepLinkTitle);
        Utilities.setTextViewText(convertView, R.id.deep_link_package_name, deepLinkInfo.getPackageName());
        Utilities.setTextViewText(convertView, R.id.deep_link_activity_name, deepLinkInfo.getActivityLabel());
        try
        {
            Drawable icon = _context.getPackageManager().getApplicationIcon(deepLinkInfo.getPackageName());
            ((ImageView) convertView.findViewById(R.id.deep_link_icon)).setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException exception)
        {
            ((ImageView) convertView.findViewById(R.id.deep_link_icon)).setImageDrawable(_context.getResources().getDrawable(R.drawable.ic_launcher));
        }
        convertView.findViewById(R.id.deep_link_remove).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                _originalList.remove(i);
                updateResults(_searchString);
                new DeepLinkHistory(_context).removeLinkFromHistory(deepLinkInfo.getDeepLink());
            }
        });
        return convertView;
    }

    @Override
    protected List<DeepLinkInfo> getMatchingResults(CharSequence constraint)
    {
        List<DeepLinkInfo> prefixList = new ArrayList<DeepLinkInfo>();
        List<DeepLinkInfo> suffixList = new ArrayList<DeepLinkInfo>();
        for (DeepLinkInfo info : _originalList)
        {
            if (info.getDeepLink().startsWith(constraint.toString()))
            {
                prefixList.add(info);
            } else if (info.getDeepLink().contains(constraint))
            {
                suffixList.add(info);
            }
        }
        prefixList.addAll(suffixList);
        return prefixList;
    }
}

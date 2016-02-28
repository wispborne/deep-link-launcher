package com.manoj.dlt.features;

import android.content.Context;
import com.manoj.dlt.Constants;
import com.manoj.dlt.interfaces.IDeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;

import java.util.ArrayList;
import java.util.List;

public class DeepLinkHistory implements IDeepLinkHistory
{
    FileSystem _fileSystem;

    public DeepLinkHistory(Context context)
    {
        _fileSystem = new FileSystem(context, Constants.DEEP_LINK_HISTORY_KEY);
    }

    @Override
    public List<DeepLinkInfo> getAllLinksSearched()
    {
        List<DeepLinkInfo> deepLinks = new ArrayList<DeepLinkInfo>();
        for(String deepLinkInfoJson: _fileSystem.values())
        {
            deepLinks.add(DeepLinkInfo.fromJson(deepLinkInfoJson));
        }
        return deepLinks;
    }

    @Override
    public void addLinkToHistory(DeepLinkInfo deepLinkInfo)
    {
        _fileSystem.write(deepLinkInfo.getId(), DeepLinkInfo.toJson(deepLinkInfo));
    }

    @Override
    public void removeLinkFromHistory(String deepLink)
    {
        _fileSystem.clear(deepLink);
    }

    @Override
    public void clearAllHistory()
    {
        _fileSystem.clearAll();
    }
}

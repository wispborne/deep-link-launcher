package com.manoj.dlt.features;

import android.content.Context;
import com.manoj.dlt.Constants;
import com.manoj.dlt.interfaces.IDeepLinkHistory;

import java.util.List;

public class DeepLinkHistory implements IDeepLinkHistory
{
    FileSystem _fileSystem;

    public DeepLinkHistory(Context context)
    {
        _fileSystem = new FileSystem(context, Constants.DEEP_LINK_HISTORY_KEY);
    }

    @Override
    public List<String> getAllLinksSearched()
    {
        return null;
    }

    @Override
    public void addLinkToHistory(String deepLink)
    {

    }

    @Override
    public void removeLinkFromHistory(String deepLink)
    {

    }

    @Override
    public void clearAllHistory()
    {

    }
}

package com.manoj.dlt.interfaces;

import com.manoj.dlt.models.DeepLinkInfo;

import java.util.List;

public interface IDeepLinkHistory
{
    public void addLinkToHistory(DeepLinkInfo deepLinkInfo);

    public void removeLinkFromHistory(String deepLink);

    public void clearAllHistory();
}

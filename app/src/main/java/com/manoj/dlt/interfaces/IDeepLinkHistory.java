package com.manoj.dlt.interfaces;

import com.manoj.dlt.models.DeepLinkInfo;

import java.util.List;

public interface IDeepLinkHistory
{
    public List<DeepLinkInfo> getAllLinksSearchedInfo();

    public List<String> getAllLinksSearched();

    public void addLinkToHistory(DeepLinkInfo deepLinkInfo);

    public void removeLinkFromHistory(String deepLink);

    public void clearAllHistory();
}

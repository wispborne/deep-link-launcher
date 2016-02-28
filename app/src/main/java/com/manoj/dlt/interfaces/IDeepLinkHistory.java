package com.manoj.dlt.interfaces;

import java.util.List;

public interface IDeepLinkHistory
{
    public List<String> getAllLinksSearched();

    public void addLinkToHistory(String deepLink);

    public void removeLinkFromHistory(String deepLink);

    public void clearAllHistory();
}

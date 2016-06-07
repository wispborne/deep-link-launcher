package com.manoj.dlt.features;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.manoj.dlt.Constants;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.events.DeepLinkFireEvent;
import com.manoj.dlt.interfaces.IDeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeepLinkHistoryFeature implements IDeepLinkHistory
{
    private static DeepLinkHistoryFeature _instance;
    private FileSystem _fileSystem;
    private Context _context;

    private DeepLinkHistoryFeature(Context context)
    {
        _fileSystem = new FileSystem(context, Constants.DEEP_LINK_HISTORY_KEY);
        EventBus.getDefault().register(this);
        _context = context;
    }

    public static DeepLinkHistoryFeature getInstance(Context context)
    {
        if(_instance == null)
        {
            _instance = new DeepLinkHistoryFeature(context);
        }
        return _instance;
    }

    @Override
    public List<DeepLinkInfo> getAllLinksSearchedInfo()
    {
        //TODO: read from firebase
        List<DeepLinkInfo> deepLinks = new ArrayList<DeepLinkInfo>();
        for (String deepLinkInfoJson : _fileSystem.values())
        {
            deepLinks.add(DeepLinkInfo.fromJson(deepLinkInfoJson));
        }
        Collections.sort(deepLinks);
        return deepLinks;
    }

    @Override
    public List<String> getAllLinksSearched()
    {
        return _fileSystem.keyList();
    }

    @Override
    public void addLinkToHistory(DeepLinkInfo deepLinkInfo)
    {
        DatabaseReference baseUserReference = ProfileFeature.getInstance(_context).getCurrentUserFirebaseBaseRef();
        DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY).child(deepLinkInfo.getId());
        linkReference.child(DbConstants.DL_ACTIVITY_LABEL).setValue(deepLinkInfo.getActivityLabel());
        linkReference.child(DbConstants.DL_DEEP_LINK).setValue(deepLinkInfo.getDeepLink());
        linkReference.child(DbConstants.DL_PACKAGE_NAME).setValue(deepLinkInfo.getPackageName());
        linkReference.child(DbConstants.DL_UPDATED_TIME).setValue(deepLinkInfo.getUpdatedTime());

        //TODO: remove this legacy code
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

    @Subscribe(sticky = true, priority = 1)
    public void onEvent(DeepLinkFireEvent deepLinkFireEvent)
    {
        addLinkToHistory(deepLinkFireEvent.getDeepLinkInfo());
    }
}

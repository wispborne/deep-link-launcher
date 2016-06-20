package com.manoj.dlt.features;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.manoj.dlt.Constants;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.events.DeepLinkFireEvent;
import com.manoj.dlt.interfaces.IDeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.models.ResultType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepLinkHistoryFeature implements IDeepLinkHistory
{
    private static DeepLinkHistoryFeature _instance;
    private FileSystem _fileSystem;
    private Context _context;

    private DeepLinkHistoryFeature(Context context)
    {
        _fileSystem = new FileSystem(context, Constants.DEEP_LINK_HISTORY_KEY);
        _context = context;
        migrateHistoryToFirebase();
        EventBus.getDefault().register(this);
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
    public void addLinkToHistory(final DeepLinkInfo deepLinkInfo)
    {
        DatabaseReference baseUserReference = ProfileFeature.getInstance(_context).getCurrentUserFirebaseBaseRef();
        DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY).child(deepLinkInfo.getId());
        Map<String, Object> infoMap = new HashMap<String, Object>(){{
        put(DbConstants.DL_ACTIVITY_LABEL, deepLinkInfo.getActivityLabel());
        put(DbConstants.DL_DEEP_LINK, deepLinkInfo.getDeepLink());
        put(DbConstants.DL_PACKAGE_NAME, deepLinkInfo.getPackageName());
        put(DbConstants.DL_UPDATED_TIME, deepLinkInfo.getUpdatedTime());
        }};
        linkReference.setValue(infoMap);
    }

    @Override
    public void removeLinkFromHistory(String deepLinkId)
    {
        DatabaseReference baseUserReference = ProfileFeature.getInstance(_context).getCurrentUserFirebaseBaseRef();
        DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY).child(deepLinkId);
        linkReference.setValue(null);
    }

    @Override
    public void clearAllHistory()
    {
        DatabaseReference baseUserReference = ProfileFeature.getInstance(_context).getCurrentUserFirebaseBaseRef();
        DatabaseReference historyRef = baseUserReference.child(DbConstants.USER_HISTORY);
        historyRef.setValue(null);
    }

    @Subscribe(sticky = true, priority = 1)
    public void onEvent(DeepLinkFireEvent deepLinkFireEvent)
    {
        if(deepLinkFireEvent.getResultType().equals(ResultType.SUCCESS))
        {
            addLinkToHistory(deepLinkFireEvent.getDeepLinkInfo());
        }
    }

    //legacy function to migrate db to firebase, for older version
    //TODO: remove once all users migrated
    private void migrateHistoryToFirebase()
    {
        for(DeepLinkInfo info: getLinkHistoryFromFileSystem())
        {
            addLinkToHistory(info);
        }
        _fileSystem.clearAll();
    }

    private List<DeepLinkInfo> getLinkHistoryFromFileSystem()
    {
        List<DeepLinkInfo> deepLinks = new ArrayList<DeepLinkInfo>();
        for (String deepLinkInfoJson : _fileSystem.values())
        {
            deepLinks.add(DeepLinkInfo.fromJson(deepLinkInfoJson));
        }
        Collections.sort(deepLinks);
        return deepLinks;
    }
}

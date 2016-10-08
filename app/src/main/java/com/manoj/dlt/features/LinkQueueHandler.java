package com.manoj.dlt.features;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manoj.dlt.Constants;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.utils.FirebaseChildAddedListener;
import com.manoj.dlt.utils.Utilities;

public class LinkQueueHandler {
    private static LinkQueueHandler _QHandler;

    private Context _context;
    private boolean _isProcessing;
    private ChildEventListener _queueListener;
    private DatabaseReference _queueReference;

    private LinkQueueHandler(Context context)
    {
        _context = context;
        _isProcessing = false;
        _queueReference = ProfileFeature.getInstance(_context).getCurrentUserFirebaseBaseRef().child(DbConstants.LINK_QUEUE);
        _queueListener = getQueueListener();
    }

    public static LinkQueueHandler getInstance(Context context)
    {
        if(_QHandler == null)
        {
            _QHandler = new LinkQueueHandler(context);
        }
        return _QHandler;
    }

    public void runQueueListener()
    {
        if(_isProcessing)
        {
            //Already attached listener on queue. do nothing
            return;
        } else if(Constants.isFirebaseAvailable(_context))
        {
            _queueReference.addChildEventListener(_queueListener);
            _isProcessing = true;
        }
    }

    public void stopQueueListener()
    {
        if(_isProcessing)
        {
            _queueReference.removeEventListener(_queueListener);
            _isProcessing = false;
        }
    }

    @NonNull
    private FirebaseChildAddedListener getQueueListener() {
        return new FirebaseChildAddedListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                String qId = dataSnapshot.getKey();
                String deepLink = dataSnapshot.getValue().toString();
                Utilities.checkAndFireDeepLink(deepLink, _context);
                Utilities.logLinkViaWeb(deepLink, ProfileFeature.getInstance(_context).getUserId(), _context);
                _queueReference.child(qId).setValue(null);
            }
        };
    }
}

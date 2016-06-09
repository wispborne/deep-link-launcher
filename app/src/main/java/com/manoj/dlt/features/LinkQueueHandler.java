package com.manoj.dlt.features;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.utils.FirebaseChildAddedListener;
import com.manoj.dlt.utils.Utilities;

public class LinkQueueHandler {
    private static LinkQueueHandler _QHandler;

    private Context _context;
    private boolean _isProcessing;

    private LinkQueueHandler(Context context)
    {
        _context = context;
        _isProcessing = false;
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
        } else
        {
            DatabaseReference baseUserReference = ProfileFeature.getInstance(_context).getCurrentUserFirebaseBaseRef();
            final DatabaseReference queueReference = baseUserReference.child(DbConstants.LINK_QUEUE);
            queueReference.addChildEventListener(new FirebaseChildAddedListener()
            {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                {
                    String qId = dataSnapshot.getKey();
                    String deepLink = dataSnapshot.getValue().toString();
                    Utilities.checkAndFireDeepLink(deepLink, _context);
                    queueReference.child(qId).setValue(null);
                }
            });
            _isProcessing = true;
        }
    }
}

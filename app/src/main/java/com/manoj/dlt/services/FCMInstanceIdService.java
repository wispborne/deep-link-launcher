package com.manoj.dlt.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.features.ProfileFeature;

public class FCMInstanceIdService extends FirebaseInstanceIdService
{
    private static final String TAG = "FCM";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        //dumb registration id to firebase
        ProfileFeature.getInstance(getApplicationContext()).getCurrentUserFirebaseBaseRef()
                .child(DbConstants.FCM_TOKEN).setValue(refreshedToken);
    }
}

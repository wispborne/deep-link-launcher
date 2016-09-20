package com.manoj.dlt;

import android.app.Application;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.manoj.dlt.features.DeepLinkHistoryFeature;
import com.manoj.dlt.features.LinkQueueHandler;
import com.manoj.dlt.features.ProfileFeature;
import com.manoj.dlt.utils.Utilities;
import io.fabric.sdk.android.Fabric;

public class DeepLinkTestApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        if(Constants.ENVIRONMENT.equals(Constants.CONFIG.PRODUCTION))
        {
            Fabric.with(this, new Crashlytics());
            Crashlytics.setUserIdentifier(ProfileFeature.getInstance(this).getUserId());
        } else
        {
            Toast.makeText(getApplicationContext(),"In Testing mode",Toast.LENGTH_LONG).show();
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        DeepLinkHistoryFeature.getInstance(getApplicationContext());
        Utilities.initializeAppRateDialog(getApplicationContext());
        LinkQueueHandler.getInstance(getApplicationContext()).runQueueListener();
    }
}

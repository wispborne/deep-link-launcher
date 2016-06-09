package com.manoj.dlt;

import android.app.Application;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.manoj.dlt.features.LinkQueueHandler;
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
        } else
        {
            Toast.makeText(getApplicationContext(),"In Testing mode",Toast.LENGTH_LONG).show();
        }
        Utilities.initializeAppRateDialog(getApplicationContext());
        LinkQueueHandler.getInstance(getApplicationContext()).runQueueListener();
    }
}

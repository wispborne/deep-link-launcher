package com.manoj.dlt;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.manoj.dlt.utils.Utilities;
import io.fabric.sdk.android.Fabric;

public class DeepLinkTestApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Utilities.initializeAppRateDialog(getApplicationContext());
    }
}

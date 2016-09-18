package com.manoj.dlt;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants
{
    public enum CONFIG
    {
        SANDBOX, PRODUCTION;
    }

    public static final String DEEP_LINK_HISTORY_KEY = "deep_link_history_key_v1";
    public static final String GLOBAL_PREF_KEY = "one_time_key";
    public static final String APP_TUTORIAL_SEEN = "app_tut_seen";
    public static final String USER_ID_KEY = "user_id";
    public static final String GOOGLE_PLAY_URI = "https://play.google.com/store/apps/details?id=com.manoj.dlt";
    public static CONFIG ENVIRONMENT  = CONFIG.SANDBOX;


    public static DatabaseReference getFirebaseUserRef()
    {
        return FirebaseDatabase.getInstance()
                .getReference(ENVIRONMENT.name().toLowerCase())
                .child(DbConstants.USERS);
    }

    public static boolean isFirebaseAvailable(Context context)
    {
        int playServicesAvl = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if(playServicesAvl == ConnectionResult.SUCCESS)
        {
            return true;
        } else
        {
            return false;
        }
    }
}

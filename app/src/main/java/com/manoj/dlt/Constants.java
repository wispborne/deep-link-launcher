package com.manoj.dlt;

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
    public static CONFIG ENVIRONMENT  = CONFIG.SANDBOX;

    private static final String FIREBASE_BASE_REF = "https://sweltering-fire-2158.firebaseio.com/";

    public static String getFirebaseUserRef()
    {
        return FIREBASE_BASE_REF.concat("/"+ENVIRONMENT.name().toLowerCase()).concat("/users");
    }
}

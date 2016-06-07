package com.manoj.dlt;

public class Constants
{
    public enum ENVIRONMENT
    {
        SANDBOX, PRODUCTION;
    }

    public static final String DEEP_LINK_HISTORY_KEY = "deep_link_history_key_v1";
    public static final String ONE_TIME_PREF_KEY = "one_time_key";
    public static final String APP_TUTORIAL_SEEN = "app_tut_seen";

    private static ENVIRONMENT _environment = ENVIRONMENT.SANDBOX;
    private static final String USER_HISTORY_BASE = "https://sweltering-fire-2158.firebaseio.com/";

    public static String getUserHistoryBase()
    {
        return USER_HISTORY_BASE.concat(_environment.name().toLowerCase()).concat("users");
    }
}

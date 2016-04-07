package com.manoj.dlt.models;

import android.net.Uri;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class DeepLinkInfo implements Comparable
{
    private String _activitylabel;
    private String _packageName;
    private String _deepLink;
    private String _id;
    private long _updatedTime; //Milliseconds

    public DeepLinkInfo(String deepLink, String activityLabel, String packageName, long updatedTime)
    {
        _activitylabel = activityLabel;
        _packageName = packageName;
        _deepLink = deepLink;
        _updatedTime = updatedTime;
        _id = generateId();
    }

    //Deep link without params itself is the unique identifier for the model
    public String getId()
    {
        return _id;
    }

    public String getPackageName()
    {
        return _packageName;
    }

    public String getActivityLabel()
    {
        return _activitylabel;
    }

    public String getDeepLink()
    {
        return _deepLink;
    }

    public long getUpdatedTime()
    {
        return _updatedTime;
    }

    public static String toJson(DeepLinkInfo deepLinkInfo)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_KEYS.KEY_DEEP_LINK, deepLinkInfo.getDeepLink());
            jsonObject.put(JSON_KEYS.KEY_ACTIVITY_LABEL, deepLinkInfo.getActivityLabel());
            jsonObject.put(JSON_KEYS.KEY_PACKAGE_NAME, deepLinkInfo.getPackageName());
            jsonObject.put(JSON_KEYS.KEY_UPDATED_TIME, deepLinkInfo.getUpdatedTime());
            return jsonObject.toString();
        } catch (JSONException jsonException)
        {
            return deepLinkInfo.getId();
        }
    }

    public static DeepLinkInfo fromJson(String deepLinkJson)
    {
        Log.d("deeplink","json string = "+deepLinkJson);
        try
        {
            JSONObject jsonObject = new JSONObject(deepLinkJson);
            String deepLink = jsonObject.getString(JSON_KEYS.KEY_DEEP_LINK);
            String activityLable = jsonObject.getString(JSON_KEYS.KEY_ACTIVITY_LABEL);
            String packageName = jsonObject.getString(JSON_KEYS.KEY_PACKAGE_NAME);
            long updatedTime = jsonObject.getLong(JSON_KEYS.KEY_UPDATED_TIME);
            return new DeepLinkInfo(deepLink, activityLable, packageName, updatedTime);
        } catch (JSONException jsonException)
        {
            Log.d("deeplink","returning null for deep lnk info, exception = "+jsonException);
            return null;
        }
    }

    @Override
    public int compareTo(Object o)
    {
        if (o == null || !(o instanceof DeepLinkInfo))
        {
            return -1;
        }
        DeepLinkInfo that = (DeepLinkInfo) o;
        if (this.getUpdatedTime() < that.getUpdatedTime())
        {
            return 1;
        } else
        {
            return -1;
        }
    }

    //unique id for each deep link entry. similar deep links, varying in query or fragments are combined
    private String generateId()
    {
        Uri uri = Uri.parse(_deepLink);
        String id = uri.toString();
        if (uri.getFragment() != null)
        {
            id = id.replace(uri.getFragment(), "").replace("#", "");
        }
        if (uri.getQuery() != null)
        {
            id = id.replace(uri.getQuery(), "").replace("?", "");
        }
        id = id.replace("/","");
        return id;
    }

    private static class JSON_KEYS
    {
        public static String KEY_DEEP_LINK = "deep_link";
        public static String KEY_PACKAGE_NAME = "pacakage_name";
        public static String KEY_ACTIVITY_LABEL = "label";
        public static String KEY_UPDATED_TIME = "update_time";
    }
}
